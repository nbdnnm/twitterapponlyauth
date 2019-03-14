package io.fake.twitterapponlyauth

import io.qameta.allure.restassured.AllureRestAssured
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.builder.RequestSpecBuilder
import io.restassured.path.json.JsonPath.from
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.not
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.lang.System.getProperty

val correctAuthorization = mapOf(
    "Authorization" to "Basic " + getProperty("authHeader"),
    "content-type" to "application/x-www-form-urlencoded;charset=UTF-8"
)
val twitterApiURL = "https://api.twitter.com"
val authTokenURI = "/oauth2/token"
var token = given()
    .headers(correctAuthorization)
    .body("grant_type=client_credentials")
    .post(twitterApiURL + "/oauth2/token")
    .then()
    .extract()
    .response()
    .path<String>("access_token")

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TwitterTest {

    @BeforeAll
    fun setUp(){
        val reqSpec = RequestSpecBuilder()
            .addFilter(AllureRestAssured())
            .build()
        RestAssured.requestSpecification = reqSpec
    }

    @Test
    fun `Positive authentication`() {

        given()
            .headers(correctAuthorization)
            .body("grant_type=client_credentials")
            .post(twitterApiURL + authTokenURI)
            .then()
            .assertThat()
            .statusCode(200)
            .and()
            .body(containsString("token_type"))
            .and()
            .body(containsString("access_token"))
    }

    @Test
    fun `Negative authentication`() {
        val authorization = mapOf(
            "correctAuthorization" to "Basic WHATEVERWRONG==",
            "content-type" to "application/x-www-form-urlencoded;charset=UTF-8"
        )

        given()
            .headers(authorization)
            .body("grant_type=client_credentials")
            .post(twitterApiURL + authTokenURI)
            .then()
            .assertThat()
            .statusCode(403)
            .and()
            .body(not(containsString("token_type")))
            .and()
            .body(not(containsString("access_token")))
    }

    @Test
    fun `Invalid input`() {
        given()
            .headers(correctAuthorization)
            .body("grant_type=WRONGINPUT")
            .post(twitterApiURL + authTokenURI)
            .then()
            .assertThat()
            .statusCode(400)
            .and()
            .body(not(containsString("token_type")))
            .and()
            .body(not(containsString("access_token")))
    }

    @Test
    fun `Check rate limit`() {
        val whichApiToTest = "/statuses/user_timeline"
        val jsonPathToRemainingApiCalls = "resources.statuses.\"$whichApiToTest\".remaining"
        val uriToRateLimitCheck = "/1.1/application/rate_limit_status.json"

        val initialLimit = from(given()
            .headers(mapOf("Authorization" to "Bearer " + token))
            .get(twitterApiURL + uriToRateLimitCheck)
            .then()
            .extract()
            .body().asString()).getInt(jsonPathToRemainingApiCalls)

        given()
            .headers(mapOf("Authorization" to "Bearer " + token))
            .get(twitterApiURL+"/1.1$whichApiToTest.json?count=100&screen_name=twitterapi")

        val limitAfterRequest = from(given()
            .headers(mapOf("Authorization" to "Bearer " + token))
            .get(twitterApiURL + uriToRateLimitCheck)
            .then()
            .extract()
            .body().asString()).getInt(jsonPathToRemainingApiCalls)

        assertEquals(initialLimit - 1, limitAfterRequest, "Limit was not decreased")
    }
}
