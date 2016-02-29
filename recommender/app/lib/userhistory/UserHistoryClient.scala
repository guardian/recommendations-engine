package lib.userhistory

import lib.userhistory.models.ActionWrapper
import play.api.Play.current
import play.api.libs.ws.{WS, WSResponse}

import scala.concurrent.{ExecutionContext, Future}

class UserHistoryClient(apiKey: String, base: String)(implicit val ec: ExecutionContext) {
  def articlesForBrowser(browserId: String): Future[List[String]] = {
    WS.url(s"$base/actions?browserId=$browserId")
      .withHeaders("Authorization" -> s"Bearer token=$apiKey")
      .get()
      .map(articlesFromResponse)
  }

  private def articlesFromResponse(response: WSResponse) =
    response.json.as[ActionWrapper].data.actions.map(_.url.replace("http://www.theguardian.com/",""))
}
