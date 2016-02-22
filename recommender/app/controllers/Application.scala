package controllers

import models._
import org.joda.time.DateTime
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import lib.{RecommendationClient, ItemHydrator}
import data.RecommenderConfiguration._
import play.api.mvc.BodyParsers.parse.{json => BodyJson}

object Application extends Controller {

  val recommendationsClient = new RecommendationClient(predictionioBase)

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

  def recommendationsFromUserId(
    userId: String,
    webPublicationDate: Option[DateRangeFilter],
    tags: Option[QueryBoost],
    disableDateFilter: Option[Boolean],
    pageSize: Option[Int]
  ) = Action.async {
    val dateFilter =
      webPublicationDate
      .orElse(Some(defaultDateRangeFilter))
      .filterNot(_ => disableDateFilter.contains(true))

    val num = pageSize orElse Some(defaultPageSize)

    for {
      recommendations <- recommendationsClient.getRecommendations(Some(userId), tags.toList, dateFilter, num)
      hydratedRecommendations <- hydrateRecommendations(recommendations)
    } yield {
      val contentJson = hydratedRecommendations.mkString("[", ",", "]")
      Ok(s"""{"content": $contentJson}""")
    }
  }

  def recommendationsFromArticleIds() = Action.async(BodyJson[ApiRequest]) { request =>
    val apiRequest = request.body
    val dateFilter = apiRequest.webPublicationDate
      .orElse(Some(defaultDateRangeFilter))
      .filterNot(_ => apiRequest.disableDateFilter.contains(true))

    val num = apiRequest.pageSize orElse Some(defaultPageSize)

    val articles = QueryBoost(name = "view", values = apiRequest.articles, bias = 1.0f)

    for {
      recommendations <- recommendationsClient.getRecommendations(None, articles :: apiRequest.tags.toList, dateFilter, num, Some(apiRequest.articles))
      hydratedRecommendations <- hydrateRecommendations(recommendations)
    } yield {
      val contentJson = hydratedRecommendations.mkString("[", ",", "]")
      Ok( s"""{"content": $contentJson}""")
    }
  }

}
