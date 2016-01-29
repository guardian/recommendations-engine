package models

import org.joda.time.DateTime
import org.specs2.mutable.Specification
import play.api.libs.json.{JsString, Json}

class DateRangeFilterTest extends Specification {
  "DateRangeFilter" should {
    "Render a DateTime as yyyy-MM-dd'T'HH:mm:ss'Z'" in {
      val range = DateRangeFilter("testRange", Some(new DateTime("2015-01-02T03:04:05")), Some(new DateTime("2015-02-02T03:04:05")))
      (Json.toJson(range) \ "before").toOption must beSome(JsString("2015-01-02T03:04:05Z"))
      (Json.toJson(range) \ "after").toOption must beSome(JsString("2015-02-02T03:04:05Z"))
    }
  }
}
