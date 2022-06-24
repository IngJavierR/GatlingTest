package microservice

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class SaveUserSimulation extends Simulation {
  val config = ConfigFactory.load("application")
  var baseUrl = config.getString("url.baseUrl")
  var baseAuthUrl = config.getString("url.baseAuthUrl")
  var username = config.getString("oauth2.username")
  var password = config.getString("oauth2.password")
  var client_id = config.getString("oauth2.client_id")
  var client_secret = config.getString("oauth2.client_secret")

  val httpProtocol = http
    .baseUrl(baseUrl)
    .inferHtmlResources(AllowList(), DenyList())
    .acceptHeader("*/*")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("PostmanRuntime/7.28.4")

  var token = ""
  val headersLogin = Map(
    "Cache-Control" -> "no-cache",
    "Postman-Token" -> "a435098d-aa46-4824-970f-756a972d44ce")

  val feeder = csv("users.csv").random

  val auth = scenario("GetToken")
      .exec(http("authenticate")
        .post(baseAuthUrl + "/auth/realms/master/protocol/openid-connect/token")
        .headers(headersLogin)
        .formParam("grant_type", "password")
        .formParam("username", username)
        .formParam("password", password)
        .formParam("client_id", client_id)
        .formParam("client_secret", client_secret)
        .check(status.is(200))
        .check(jsonPath("$.access_token").saveAs("access")))
        .exec{session => { token = session("access").as[String]
          session}}

  object BusinessLogic {
    var headers_10 = Map("Content-Type" -> "application/json; charset=ISO-8859-1",
      "Authorization" -> "Bearer ${token}")
    val saveUsers =
      feed(feeder)
        .exec(http("SaveUser")
        .post("/stress/users")
        .headers(headers_10.toMap)
        .body(StringBody("""{"id": "#{Id}","name": "#{Name}","lastName": "#{LastName}","age": "#{Age}"}"""))
        .check(status.is(200))
      )
  }

  val scn = scenario("SaveUsers")
    .exec(session => session.set("access", token))
    .exec(
      BusinessLogic.saveUsers
    )

  setUp(
    auth.inject(constantUsersPerSec(1) during (1 seconds)),
    scn.inject(
      nothingFor(2 seconds),
      atOnceUsers(2),
      rampUsers(1) during (20)
  )).protocols(httpProtocol)
}
