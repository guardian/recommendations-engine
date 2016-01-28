package data

import com.gu.conf.ConfigurationFactory

object RecommenderConfiguration {
  private val conf = ConfigurationFactory.getConfiguration("mobile-recommender", "gu-conf")

  val predictionioBase = conf.getStringProperty("apis.predictionio.base", "")

  val mobileItems = conf.getStringProperty("apis.mobile-api.items.base", "")

  val defaultRecommendationsCutoffDays = conf.getIntegerProperty("defaultRecommendationsCutoffDays", 7)

  val defaultPageSize = conf.getIntegerProperty("defaultPageSize", 10)
}

