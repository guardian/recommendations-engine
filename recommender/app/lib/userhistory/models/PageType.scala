package lib.userhistory.models

import play.api.data.validation.ValidationError
import play.api.libs.json.{JsString, JsResult, JsValue, Format}

sealed trait PageType {
  def asString: String
}

object PageType {
  implicit val jf = new Format[PageType] {
    override def reads(json: JsValue): JsResult[PageType] = json.validate[String].collect(ValidationError("Unrecognised page type")) {
      case Front.asString => Front
      case Article.asString => Article
    }

    override def writes(o: PageType): JsValue =
      JsString(o.asString)
  }
}

case object Front extends PageType {
  val asString = "Front"
}

case object Article extends PageType {
  val asString = "Article"
}
