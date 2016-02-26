package data

import com.gu.conf.ConfigurationFactory
import lib.ElasticSearchDiscovery
import play.Logger

object RecommenderConfiguration extends ElasticSearchDiscovery {
  private val conf = ConfigurationFactory.getConfiguration("recommender", "gu-conf")

  val predictionioBase = conf.getStringProperty("apis.predictionio.base", "")

  val mobileItems = conf.getStringProperty("apis.mobile-api.items.base", "")

  val defaultRecommendationsCutoffDays = conf.getIntegerProperty("defaultRecommendationsCutoffDays", 7)

  val defaultPageSize = conf.getIntegerProperty("defaultPageSize", 10)

  val region = conf.getStringProperty("aws.region", "eu-west-1")

  val app = conf.getStringProperty("elasticsearch.app", "elasticsearch")

  val stack = conf.getStringProperty("stage", "recommendations")

  val stage = conf.getStringProperty("stage", "PROD")

  val clusterName = s"recommendations-$stage"

  val staticElasticSearchHosts = conf.getStringProperty("elasticsearch.hosts").map(_.split(',').toList)

  def elasticsearchHosts: List[String] = {
    val hosts = staticElasticSearchHosts getOrElse findElasticsearchHostsInEc2
    Logger.info(s"Elasticsearch hosts: $hosts")
    hosts
  }
}

