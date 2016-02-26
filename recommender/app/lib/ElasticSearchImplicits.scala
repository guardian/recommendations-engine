package lib

import models.DateRangeFilter
import org.elasticsearch.action.{ActionListener, ListenableActionFuture}
import org.elasticsearch.index.query.{QueryBuilder, BoolQueryBuilder, QueryBuilders}
import org.joda.time.{DateTimeZone, DateTime}
import play.Logger

import scala.concurrent.Promise

object ElasticSearchImplicits {
  implicit class EnrichedDateRangeFilter(filter: DateRangeFilter) {
    def asMust: BoolQueryBuilder => BoolQueryBuilder = _.must(asRangeQuery)

    def asRangeQuery = {
      def formatDate(d: DateTime) = d.withZone(DateTimeZone.UTC).toString("yyyy-MM-dd'T'HH:mm:ss'Z'")

      val before = filter.before.map(formatDate)
      val after = filter.after.map(formatDate)

      QueryBuilders.rangeQuery(filter.name)
        .optAndThen(qb => after.map(qb.gt))
        .optAndThen(qb => before.map(qb.lt))
    }
  }

  implicit class EnrichedQueryBuilder[T <: QueryBuilder](qb: T) {
    def andThen(fn: T => T): T = fn(qb)
    def optAndThen(fn: T => Option[T]): T = fn(qb).getOrElse(qb)
    def optAndThen(optFn: Option[T => T]): T = optFn.map(_(qb)).getOrElse(qb)
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
