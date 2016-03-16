package lib.userhistory.models

import play.api.data.validation.ValidationError
import play.api.libs.json.{JsString, JsResult, JsValue, Format}

sealed trait Device {
  def asString: String
}

object Device {
  implicit val jf = new Format[Device] {
    override def reads(json: JsValue): JsResult[Device] = json.validate[String].collect(ValidationError("Unrecognised device")) {
      case Android.asString => Android
      case IOS.asString => IOS
      case Browser.asString => Browser
    }

    override def writes(o: Device): JsValue =
      JsString(o.asString)
  }
}

case object Android extends Device {
  val asString = "Android"
}

case object IOS extends Device {
  val asString = "IOS"
}

case object Browser extends Device {
  val asString = "Browser"
}
