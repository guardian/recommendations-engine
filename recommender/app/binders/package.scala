import models.{QueryBoost, DateRangeFilter}
import org.joda.time.DateTime
import play.api.mvc.QueryStringBindable

import scala.language.implicitConversions
import scala.util.{Try, Success, Failure}

package object binders {

  implicit def bindDateTime(implicit strBinder: QueryStringBindable[String]): QueryStringBindable[DateTime] =
    new QueryStringBindable[DateTime] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String,DateTime]] = {
        strBinder.bind(key, params) map {
          _.right.flatMap { v =>
            Try {
              DateTime.parse(v)
            } match {
              case Success(date) => Right(date)
              case Failure(error) => Left(error.getMessage)
            }
          }
        }
      }

      override def unbind(key: String, value: DateTime): String = strBinder.unbind(
        key = key,
        value = value.toString
      )
    }

  implicit def bindDateRangeFilter(implicit dateBinder: QueryStringBindable[DateTime]) =
    new QueryStringBindable[DateRangeFilter] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, DateRangeFilter]] =
        optionEitherSwap {
          for {
            before <- optionEitherSwap(dateBinder.bind(s"$key.before", params)).right
            after <- optionEitherSwap(dateBinder.bind(s"$key.after", params)).right
          } yield before orElse after map { _ => DateRangeFilter(key, before, after) }
        }

      override def unbind(key: String, filter: DateRangeFilter) = List(
        filter.before map { before => s"${dateBinder.unbind(s"$key.before", before)}" },
        filter.after map { after => s"${dateBinder.unbind(s"$key.before", after)}" }
      ).mkString("&")

      private def optionEitherSwap[T, U](a: Option[Either[T, U]]): Either[T, Option[U]] = a match {
        case Some(Right(x)) => Right(Some(x))
        case Some(Left(y)) => Left(y)
        case None => Right(None)
      }

      private def optionEitherSwap[T, U](a: Either[T, Option[U]]): Option[Either[T, U]] = a match {
        case Right(Some(x)) => Some(Right(x))
        case Left(y) => Some(Left(y))
        case Right(None) => None
      }
    }

  implicit def bindQueryBoost(implicit listBinder: QueryStringBindable[List[String]], floatBinder: QueryStringBindable[Float]) =
    new QueryStringBindable[QueryBoost] {
      override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, QueryBoost]] = {
        listBinder.bind(key, params).filterNot(emptyList) map { boundValues =>
          for {
            values <- boundValues.right
            bias <- getBias(key, params)
          } yield QueryBoost(
            name = key,
            values = values,
            bias = bias
          )
        }
      }

      override def unbind(key: String, qb: QueryBoost) =
        listBinder.unbind(key, qb.values) + "&" + floatBinder.unbind(s"$key.bias", qb.bias)

      private def emptyList(l: Either[String, List[String]]) = l.right.exists(_.isEmpty)

      private def getBias(key: String, params: Map[String, Seq[String]]) =
        floatBinder.bind(s"$key.bias", params).getOrElse(Right(-1.0f)).right
    }
}
