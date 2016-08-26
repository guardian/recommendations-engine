package recommender

import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.{Calendar, Properties}

import io.gatling.core.Predef._
import io.gatling.http.Predef._

import scala.util.Random
import scala.util.parsing.json._
import scala.concurrent.duration._
import scalaj.http.Http


class BasicSimulation extends Simulation {
  val baseUrl = "http://engine.mobile.guardianapis.com"
  //val baseUrl = "http://localhost:9000"

  val properties = new Properties()
  properties.load(new FileInputStream("dev.properties"))

  val todayArticlesResponse = Http("http://content.guardianapis.com/search")
    .param("from-date", new SimpleDateFormat("y-M-d").format(Calendar.getInstance().getTime()))
    .param("page-size", "50")
    .param("api-key", properties.getProperty("capi.api_key"))
    .asString
  val todayArticlesJson = JSON.parseFull(todayArticlesResponse.body)

  val response = todayArticlesJson.get.asInstanceOf[Map[String, Any]]("response")
  val resultsArray = response.get.asInstanceOf[Map[String, Any]]("results")

  val todayArticleIds = for {
    result <- resultsArray.get.asInstanceOf[List[Map[String, Any]]]
    id <- result.get("id")
  } yield {
    id.asInstanceOf[String]
  }

  def randomTodayArticleIds(): List[String] = {
    // The -1 +1 ensures there's always at least one item in the result
    Random.shuffle(todayArticleIds.get).take(Random.nextInt(10) + 40)
  }

  def randomBody(): String =
    randomTodayArticleIds().mkString("""{"articles":["""", """","""", """"]}""")

  val httpConf = http
    .baseURL(baseUrl) // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:48.0) Gecko/20100101 Firefox/48.0")

  val scn = scenario("get recommendations") // A scenario is a chain of requests and pauses
    .exec(http("request 1")
    .post("/recommendations")
    .body(StringBody(randomBody()))
    .asJSON
    .check(status.is(200))
    .check(jsonPath("$.content"))
  )

  //setUp(scn.inject(atOnceUsers(1)).protocols(httpConf))
  setUp(scn.inject(
    rampUsersPerSec(1) to(75) during(30 seconds),
    constantUsersPerSec(75) during(300 seconds) randomized
  )).protocols(httpConf)
}
