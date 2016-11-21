package controllers

import lib.userhistory.UserHistoryClient
import models._
import org.joda.time.DateTime
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import data.RecommenderConfiguration
import lib.{Auth, MetricSender, ItemHydrator, Recommender}
import data.RecommenderConfiguration._
import play.api.mvc.BodyParsers.parse.{json => BodyJson}

object Application extends Controller {

  private val esClient = {
    import java.net.InetAddress
    import org.elasticsearch.client.transport.TransportClient
    import org.elasticsearch.common.settings.ImmutableSettings
    import org.elasticsearch.common.transport.InetSocketTransportAddress
    import org.elasticsearch.transport.Transport.TransportSettings

    val settings = ImmutableSettings.settingsBuilder()
      .put("cluster.name", clusterName)
      .put(TransportSettings.TRANSPORT_TCP_COMPRESS, "true").build()

    val transportAddresses = elasticsearchHosts.map { host =>
      new InetSocketTransportAddress(InetAddress.getByName(host), 9300)
    }

    new TransportClient(settings)
      .addTransportAddresses(transportAddresses: _*)
  }

  private val userHistoryClient = new UserHistoryClient(
    base = userHistory.base,
    apiKey = userHistory.apiKey
  )

  private val recommender = new Recommender(esClient, userHistoryClient)

  private val auth = new Auth()

  val metricSender = new MetricSender(recommender)

  val recommendationsCorsHeaders = List(
    "Access-Control-Allow-Origin" -> "*",
    "Access-Control-Allow-Methods" -> "POST",
    "Access-Control-Allow-Headers" -> "Content-Type, X-Requested-With"
  )

  private def defaultDateRangeFilter = DateRangeFilter(
    name = "webPublicationDate",
    after = Some(DateTime.now.minusDays(defaultRecommendationsCutoffDays))
  )

  def healthCheck = Action {
    Ok
  }

  def lastSuccessfulTrain = Action.async {
    recommender.getLastSuccessfulTrain().map {
      case Some(date) => Ok(date.toString)
      case None => InternalServerError
    }
  }

  def clusterHealthy = Action.async {
    recommender.clusterHealthy().map {
      case true => Ok("cluster healthy")
      case false => Ok("cluster unhealthy")
    }
  }

  def hydrateRecommendation(recommendation: RecommendationItems) = {
    ItemHydrator.item(recommendation.item) map { optItem =>
      optItem.map { item => s"""{"score":${recommendation.score},"item":$item}""" }
    }
  }

  def hydrateRecommendations(recommendations: List[RecommendationItems]): Future[List[String]] =
    Future.sequence { recommendations map hydrateRecommendation } map { _.flatten }

  def linkRecommendation(recommendation: RecommendationItems) =
    s"""{"score":${recommendation.score},"item":"${RecommenderConfiguration.mobileItems}/${recommendation.item}"}"""

  def linkRecommendations(recommendations: List[RecommendationItems]): Future[List[String]] =
    Future.successful { recommendations map linkRecommendation }

  def recommendationsFromBrowserId(
    browserId: String,
    webPublicationDate: Option[DateRangeFilter],
    tags: Option[QueryBoost],
    disableDateFilter: Option[Boolean],
    optPageSize: Option[Int]
  ) = Action.async {
    val pageSize = optPageSize getOrElse defaultPageSize

    val dateFilter = if (disableDateFilter.contains(true))
      None
    else
      webPublicationDate orElse Some(defaultDateRangeFilter)

    for {
      recommendations <- recommender.getRecommendationsForBrowserId(browserId, dateFilter, pageSize)
      hydratedRecommendations <- hydrateRecommendations(recommendations)
    } yield {
      val contentJson = hydratedRecommendations.mkString("[", ",", "]")
      Ok( s"""{"content": $contentJson}""")
    }
  }

  def recommendationsForMe(
    webPublicationDate: Option[DateRangeFilter],
    tags: Option[QueryBoost],
    disableDateFilter: Option[Boolean],
    optPageSize: Option[Int]
  ) = Action.async { request =>
    val recommendations = for {
      token <- request.headers.get("GU-IdentityToken")
      userId <- auth.userIdFromIdentityToken(token)
    } yield {
      val pageSize = optPageSize getOrElse defaultPageSize

      val dateFilter = if (disableDateFilter.contains(true))
        None
      else
        webPublicationDate orElse Some(defaultDateRangeFilter)

      for {
        recommendations <- recommender.getRecommendationsForUserId(userId, dateFilter, pageSize)
        hydratedRecommendations <- hydrateRecommendations(recommendations)
      } yield {
        val contentJson = hydratedRecommendations.mkString("[", ",", "]")
        Ok( s"""{"content": $contentJson}""")
      }
    }
    recommendations getOrElse Future.successful(Forbidden)
  }

  def recommendationsOptions = Action {
    Ok.withHeaders(recommendationsCorsHeaders: _*)
  }

  def recommendations(format: Option[String]) = Action.async(BodyJson[ApiRequest]) { request =>
    val apiRequest = request.body
    val dateFilter = apiRequest.webPublicationDate
      .orElse(Some(defaultDateRangeFilter))
      .filterNot(_ => apiRequest.disableDateFilter.contains(true))
    val num = apiRequest.pageSize getOrElse defaultPageSize

    val formatter: List[RecommendationItems] => Future[List[String]] = format match {
      case Some("mapi_links") => linkRecommendations
      case _ => hydrateRecommendations
    }

    for {
      recommendations <- recommender.getRecommendations(apiRequest.articles, dateFilter, num)
      formattedRecommendations <- formatter(recommendations)
    } yield {
      val contentJson = formattedRecommendations.mkString("[", ",", "]")
      Ok(s"""{"content": $contentJson}""").withHeaders(recommendationsCorsHeaders: _*)
    }
  }

}