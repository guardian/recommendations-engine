package lib.userhistory.models

import play.api.libs.json.Json

object Section { implicit val jf = Json.format[Section] }
case class Section(name: String, frontsViewed: Int, articlesViewed: Int, daysVisited: Int)
