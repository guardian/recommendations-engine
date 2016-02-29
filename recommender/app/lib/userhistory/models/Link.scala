package lib.userhistory.models

import play.api.libs.json.Json

object Link { implicit val jf = Json.format[Link] }

case class Link(rel: String, href: String)
