package models

import org.joda.time.{DateTimeZone, DateTime}
import play.api.libs.json._

object JodaImplicits {
  private val DateTimePatternWithMillis = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
  private val DateTimePattern = "yyyy-MM-dd'T'HH:mm:ssZ"
  private val DateTimeWritePattern = "yyyy-MM-dd'T'HH:mm:ss'Z'"

  implicit val dateTimeReads: Reads[DateTime] = {
    Reads.jodaDateReads(DateTimePattern)
  } orElse {
    Reads.jodaDateReads(DateTimePatternWithMillis)
  } map {
    _.withZone(DateTimeZone.UTC)
  }

  implicit val dateTimeWrites = Writes[DateTime] { dt =>
    Writes.jodaDateWrites(DateTimeWritePattern).writes(dt.withZone(DateTimeZone.UTC))
  }
}