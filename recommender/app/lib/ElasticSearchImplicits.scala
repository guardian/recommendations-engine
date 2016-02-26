package lib

import models.DateRangeFilter
import org.elasticsearch.action.{ActionListener, ListenableActionFuture}
import org.elasticsearch.index.query.{RangeQueryBuilder, QueryBuilder, BoolQueryBuilder, QueryBuilders}
import org.joda.time.{DateTimeZone, DateTime}
import play.Logger

import scala.concurrent.Promise

object ElasticSearchImplicits {
  implicit class EnrichedDateRangeFilter(filter: DateRangeFilter) {
    def rangeQuery = {
      def formatDate(d: DateTime) = d.withZone(DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ss'Z'")

      QueryBuilders.rangeQuery(filter.name)
        .gt(filter.after.map(formatDate))
        .lt(filter.before.map(formatDate))
    }
  }

  implicit class EnrichedRangeQueryBuilder(rangeQuery: RangeQueryBuilder) {
    def gt(from: Option[String]) =
      from.map(a => rangeQuery.gt(a)) getOrElse rangeQuery

    def lt(from: Option[String]) =
      from.map(a => rangeQuery.lt(a)) getOrElse rangeQuery
  }

  implicit class EnrichedBoolQueryBuilder(bool: BoolQueryBuilder) {
    def should(qb: Option[QueryBuilder]) =
      qb.map(a => bool.should(a)) getOrElse bool

    def must(qb: Option[QueryBuilder]) =
      qb.map(a => bool.must(a)) getOrElse bool

    def mustNot(qb: Option[QueryBuilder]) =
      qb.map(a => bool.mustNot(a)) getOrElse bool

    def andThen(fn: BoolQueryBuilder => BoolQueryBuilder): BoolQueryBuilder =
      fn(bool)
  }

  implicit class EnrichedListenableActionFuture[T](result: ListenableActionFuture[T]) {
    def asScala = {
      val promise = Promise[T]()
      result.addListener(new ActionListener[T] {
        def onFailure(e: Throwable) {
          Logger.error("Elasticsearch query failure", e)
          promise.failure(e)
        }
        def onResponse(response: T) {
          promise.success(response)
        }
      })
      promise.future
    }
  }
}
