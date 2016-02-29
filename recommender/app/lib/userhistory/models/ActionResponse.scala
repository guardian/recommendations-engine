package lib.userhistory.models

import play.api.libs.json.Json

object ActionResponse { implicit val jf = Json.format[ActionResponse] }

case class ActionResponse(actions: List[Action])
