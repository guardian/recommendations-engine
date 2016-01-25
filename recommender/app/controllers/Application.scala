package controllers

import play.api.mvc._
import data.RecommenderConfiguration

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import lib.{RecommendationClient, ItemHydrator}

object Application extends Controller {

  val recommendationsClient = new RecommendationClient(RecommenderConfiguration.predictionioBase)

  def healthCheck = Action {
    Ok
  }

  def recommendations(userId: String) = Action.async {
    recommendationsClient.getRecommendations(userId) flatMap { recommendations =>
      val hydratedItems = Future.sequence {
        recommendations.take(10) map { item => ItemHydrator.item(item.item) }
      } map { _.flatten }

      hydratedItems.map { items =>
        val itemJson = items.mkString("[", ",", "]")
        Ok(s"""{"content": $itemJson}""")
      }
    }
  }

}
