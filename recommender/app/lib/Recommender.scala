package lib

import lib.userhistory.UserHistoryClient
import models._
import org.elasticsearch.action.search.{SearchResponse, SearchType}
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.index.query.BoolQueryBuilder
import org.elasticsearch.index.query.QueryBuilders.{termsQuery, idsQuery, boolQuery}
import org.elasticsearch.search.SearchHit
import org.elasticsearch.search.sort.{SortBuilders, SortOrder}
import org.joda.time.DateTime
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Recommender(esClient: TransportClient, userHistoryClient: UserHistoryClient) {

  import ElasticSearchImplicits._

  val esIndex = "urindex"
  val esType = "items"
  val popRank = "popRank"
  val popRankType = "double"
  val historySize = 50

  def getRecommendationsForBrowserId(browserId: String, dateFilter: Option[DateRangeFilter], pageSize: Int): Future[List[RecommendationItems]] =
    getRecommendationsForBrowserId(browserId, dateFilter, pageSize, offset = 0)

  def getRecommendationsForBrowserId(browserId: String, dateFilter: Option[DateRangeFilter], pageSize: Int, offset: Int): Future[List[RecommendationItems]] = {
    userHistoryClient.articlesForBrowser(browserId, historySize) flatMap { articleIds =>
      getRecommendations(articleIds, dateFilter, pageSize, offset)
    }
  }

  def getRecommendationsForUserId(userId: String, dateFilter: Option[DateRangeFilter], pageSize: Int): Future[List[RecommendationItems]] =
    getRecommendationsForUserId(userId, dateFilter, pageSize, offset = 0)

  def getRecommendationsForUserId(userId: String, dateFilter: Option[DateRangeFilter], pageSize: Int, offset: Int): Future[List[RecommendationItems]] = {
    userHistoryClient.articlesForUserId(userId, historySize) flatMap { articleIds =>
      getRecommendations(articleIds, dateFilter, pageSize, offset)
    }
  }

  def getRecommendations(ids: List[String], dateFilter: Option[DateRangeFilter], pageSize: Int, offset: Int = 0): Future[List[RecommendationItems]] = {
    val query = prepareQuery(ids, dateFilter)

    esClient.prepareSearch(esIndex)
      .setQuery(query)
      .setFrom(offset)
      .setSize(pageSize)
      .setTypes(esType)
      .setSearchType(SearchType.DEFAULT)
      .addSort(SortBuilders.scoreSort().order(SortOrder.DESC))
      .addSort(SortBuilders.fieldSort(popRank).unmappedType(popRankType).order(SortOrder.DESC))
      .execute().asScala map itemsFromResponse
  }

  private def prepareQuery(ids: List[String], dateFilter: Option[DateRangeFilter]) = {
    baseQuery(ids)
      .optAndThen(dateFilter.map(_.asMust))
      .andThen(filterTags)
      .andThen(addRecencyBias)
  }

  private def baseQuery(ids: List[String]) = boolQuery()
    .should(termsQuery("view", ids: _*))
    .mustNot(idsQuery().ids(ids: _*))

  private def filterTags(qb: BoolQueryBuilder) =
    qb.mustNot(termsQuery("tags", "tone/minutebyminute"))

  private def addRecencyBias(qb: BoolQueryBuilder) = {
    def daysAgo(days: Int) = DateRangeFilter(
      name = "webPublicationDate",
      after = Some(DateTime.now.minusDays(days)),
      before = Some(DateTime.now.minusDays(days - 1))
    )

    qb.should(daysAgo(1).asRangeQuery.boost(0.5f))
      .should(daysAgo(2).asRangeQuery.boost(0.2f))
      .should(daysAgo(3).asRangeQuery.boost(0.1f))
  }

  private def itemFromHit(item: SearchHit) =
    RecommendationItems(item = item.getId, score = item.getScore)

  private def itemsFromResponse(response: SearchResponse) =
    response.getHits.asScala.toList.map(itemFromHit)

}