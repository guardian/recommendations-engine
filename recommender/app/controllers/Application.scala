package controllers

import lib.userhistory.UserHistoryClient
import models._
import org.joda.time.DateTime
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import lib.{Auth, ItemHydrator, Recommender}
import data.RecommenderConfiguration
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

  private def defaultDateRangeFilter = DateRangeFilter(
    name = "webPublicationDate",
    after = Some(DateTime.now.minusDays(defaultRecommendationsCutoffDays))
  )

  def healthCheck = Action {
    Ok
  }

  def hydrateRecommendation(recommendation: RecommendationItems) = {
    ItemHydrator.item(recommendation.item) map { optItem =>
      optItem.map { item => s"""{"score":${recommendation.score},"item":$item}""" }
    }
  }

  def hydrateRecommendations(recommendations: List[RecommendationItems]) =
    Future.sequence { recommendations map hydrateRecommendation } map { _.flatten }

  def linkRecommendation(recommendation: RecommendationItems) =
    Future.successful { s"""{"score":${recommendation.score},"item":"${RecommenderConfiguration.mobileItems}/${recommendation.item}"}""" }

  def linkRecommendations(recommendations: List[RecommendationItems]) =
    Future.sequence { recommendations map linkRecommendation }

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

  def recommendations(optFormat: Option[String]) = Action.async(BodyJson[ApiRequest]) { request =>
    val apiRequest = request.body
    val dateFilter = apiRequest.webPublicationDate
      .orElse(Some(defaultDateRangeFilter))
      .filterNot(_ => apiRequest.disableDateFilter.contains(true))
    val num = apiRequest.pageSize getOrElse defaultPageSize
    val format = optFormat getOrElse "mapi_items"

    for {
      recommendations <- recommender.getRecommendations(apiRequest.articles, dateFilter, num)
      formattedRecommendations <- {
        if (format equals "mapi_links") {
          linkRecommendations(recommendations)
        } else {
          hydrateRecommendations(recommendations)
        }
      }
    } yield {
      val contentJson = formattedRecommendations.mkString("[", ",", "]")
      Ok( s"""{"content": $contentJson}""")
    }
  }

}
