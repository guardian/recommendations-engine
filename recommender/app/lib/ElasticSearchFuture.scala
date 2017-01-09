package lib

import org.elasticsearch.action.{ActionListener, ListenableActionFuture}
import play.Logger

import scala.concurrent.{Future, Promise}
import scala.util.{Success, Try, Failure}

object ElasticSearchFuture {
  def safe[T](result: => ListenableActionFuture[T]): Future[T] = {
    Try(result) match {
      case Success(r) =>
        val promise = Promise[T]()
        r.addListener(new ActionListener[T] {
          def onFailure(e: Throwable) {
            Logger.error("Elasticsearch query failure", e)
            promise.failure(e)
          }
          def onResponse(response: T) {
            promise.success(response)
          }
        })
        promise.future
      case Failure(e) =>
        Logger.error("Elasticsearch query failure", e)
        Future.failed(e)
    }
  }
}
