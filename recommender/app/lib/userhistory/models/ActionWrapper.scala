package lib.userhistory.models

import play.api.libs.json.Json

object ActionWrapper { implicit val jf = Json.format[ActionWrapper] }

case class ActionWrapper(
  uri: Option[String] = None,
  data: ActionResponse,
  links: List[Link] = Nil)
