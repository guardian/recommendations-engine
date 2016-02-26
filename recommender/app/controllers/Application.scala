package controllers

import models._
import org.joda.time.DateTime
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import lib.{Recommender, ItemHydrator}
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
      .put("cluster.name", "recommendations-PROD")
      .put(TransportSettings.TRANSPORT_TCP_COMPRESS, "true").build()

    val transportAddresses = elasticsearchHosts.map { host =>
      new InetSocketTransportAddress(InetAddress.getByName(host), 9300)
    }

    new TransportClient(settings)
      .addTransportAddresses(transportAddresses: _*)
  }

  val recommender = new Recommender(esClient)

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

  def hydrateRecommendations(recommendations: List[RecommendationItems]): Future[List[String]] =
    Future.sequence { recommendations map hydrateRecommendation } map { _.flatten }

  def recommendationsFromArticleIds() = Action.async(BodyJson[ApiRequest]) { request =>
    val apiRequest = request.body
    val dateFilter = apiRequest.webPublicationDate
      .orElse(Some(defaultDateRangeFilter))
      .filterNot(_ => apiRequest.disableDateFilter.contains(true))

    val num = apiRequest.pageSize getOrElse defaultPageSize

    for {
      recommendations <- recommender.getRecommendations(apiRequest.articles, dateFilter, num)
      hydratedRecommendations <- hydrateRecommendations(recommendations)
    } yield {
      val contentJson = hydratedRecommendations.mkString("[", ",", "]")
      Ok( s"""{"content": $contentJson}""")
    }
  }

}
