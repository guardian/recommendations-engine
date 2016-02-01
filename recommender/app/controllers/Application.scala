package controllers

import models.{RecommendationItems, DateRangeFilter, QueryBoost}
import org.joda.time.DateTime
import play.api.mvc._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import lib.{RecommendationClient, ItemHydrator}
import data.RecommenderConfiguration._

object Application extends Controller {

  val recommendationsClient = new RecommendationClient(predictionioBase)

  private val defaultDateRangeFilter = DateRangeFilter(
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

  def recommendations(
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
      recommendations <- recommendationsClient.getRecommendations(userId, tags.toList, dateFilter, num)
      hydratedRecommendations <- hydrateRecommendations(recommendations)
    } yield {
      val contentJson = hydratedRecommendations.mkString("[", ",", "]")
      Ok(s"""{"content": $contentJson}""")
    }
  }

}
