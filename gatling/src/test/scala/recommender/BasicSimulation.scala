package recommender

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._


class BasicSimulation extends Simulation {
  val baseUrl = "http://engine.mobile.guardianapis.com"
  //val baseUrl = "http://localhost:9000"

  val httpConf = http
    .baseURL(baseUrl) // Here is the root for all relative URLs
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // Here are the common headers
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Macintosh; Intel Mac OS X 10.11; rv:48.0) Gecko/20100101 Firefox/48.0")

  val scn = scenario("Get recommendations") // A scenario is a chain of requests and pauses
    .exec(http("healthcheck")
    .get("/healthcheck")
    .check(status.is(200))
  )
    /*
    .pause(20.milliseconds) // Note that Gatling has recorded real time pauses
    .exec(http("get home front")
    .get(fromOrigin("frontUri"))
    .check(jsonPath("$.layout[*]").ofType[Map[String, Any]].findAll.saveAs("layouts"))
  )
    .pause(20.milliseconds)
    .foreach("${layouts}", "layout") {
      exec({session =>
        val layoutMap = session("layout").as[Map[String, Any]]
        val containerUri = layoutMap("uri")
        session.set("containerUri", containerUri)
      }).exec(http("get container uri")
        .get(fromOrigin("containerUri"))
        .check(jsonPath("$.cards[0].item.links.relatedUri").optional.saveAs("relatedUri"))
      )
        .pause(20.milliseconds)
        .doIf(session => session.contains("relatedUri")) {
          exec(http("get related items")
            .get(fromOrigin("relatedUri"))
          )
        }

    }
    */

  setUp(scn.inject(rampUsers(50).over(30 seconds)).protocols(httpConf)) // was 100
}
