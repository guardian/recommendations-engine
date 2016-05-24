package lib

import com.gu.identity.client.IdentityApiClient
import scala.util.Try

class Auth() {

  private val identityClient = new IdentityApiClient(null, null, new com.gu.identity.cookie.ProductionKeys().publicDsaKey)

  def userIdFromIdentityToken(token: String): Option[String] = {
    Try(
      Option(identityClient.extractUserDataFromToken(token, "discussion"))
    ).toOption.flatten map { userData =>
      userData.id
    }
  }
}