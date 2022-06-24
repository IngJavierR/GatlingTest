package microservice

import com.typesafe.config.ConfigFactory
import io.gatling.core.Predef._
import io.gatling.http.Predef._

class LoginSimulation extends Simulation{

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

  object Login {
    val authenticate =
      exec(http("authenticate")
        .post(baseAuthUrl + "/auth/realms/master/protocol/openid-connect/token")
        .headers(headersLogin)
        .formParam("grant_type", "password")
        .formParam("username", username)
        .formParam("password", password)
        .formParam("client_id", client_id)
        .formParam("client_secret", client_secret)
        .check(status.is(200))
        .check(jsonPath("$.access_token").saveAs("token"))
      )
  }

  val scn = scenario("Login")
    .exec(
      Login.authenticate
    )

  setUp(scn.inject(
    atOnceUsers(1),
    rampUsers(1) during (20)
  )).protocols(httpProtocol)
}
