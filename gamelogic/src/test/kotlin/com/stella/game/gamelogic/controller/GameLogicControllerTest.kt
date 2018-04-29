package com.stella.game.gamelogic.controller

import com.github.tomakehurst.wiremock.client.WireMock
import com.stella.game.schema.gamelogic.PlayerSearchDto
import com.stella.game.schema.gamelogic.QuizResultLogDto
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.junit.Assert
import org.junit.Test

class GameLogicControllerTest : ControllerTestBase(){
    @Test
    fun testPathNotExist() {
        RestAssured.given().get("/endpointThatDoesNotExist")
                .then()
                .statusCode(401)
    }

    @Test
    fun testGetUsernameEndpoint() {
        val response = RestAssured.given()
                .auth().basic("foo", "123")
                .get("/username")

        println("response print: " + response.print())
    }

    @Test
    fun testFindOpponent_Valid(){
        val json = getMockedJson_FooAndBar()
        wiremockServerPlayer.stubFor(
                WireMock.get(
                        WireMock.urlMatching(".*/players"))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json")
                                .withBody(json)))

        val response = RestAssured.given()
                .auth().basic("foo", "123")
                .accept(ContentType.JSON)
                .get("/opponent")

        val playerSearchDto = response.`as`(PlayerSearchDto::class.java)

        Assert.assertEquals(200, response.statusCode)
        Assert.assertEquals("bar", playerSearchDto.username)
        Assert.assertEquals("2", playerSearchDto.id)

    }

    @Test
    fun testFindOpponent_PlayerNotFound() {
        // Arrange
        val json = getMockedJson_EmptyArray()
        wiremockServerPlayer.stubFor(
                WireMock.get(
                        WireMock.urlMatching(".*/players"))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json; charset=utf-8")
                                .withBody(json)))

        // Act
        val response = RestAssured
                .given()
                .auth().basic("foo", "123")
                .accept(ContentType.JSON).get("/enemy")


        // Assert
        Assert.assertEquals(404, response.statusCode)
    }

    @Test
    fun fight_TryFightYourself(){

        // Try to fight yourself

        // Arrange
        val invalidPlayerSearchDto = PlayerSearchDto("1", "foo")
        val json = getMockedJson_FooByUsername()

        wiremockServerPlayer.stubFor(
                WireMock.get(
                        WireMock.urlPathMatching(".*/players.*"))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json; charset=utf-8")
                                .withBody(json)))

        // Act
        val response = RestAssured
                .given()
                .auth().basic("foo", "123")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(invalidPlayerSearchDto)
                .post("/startRound")

        // Act
        Assert.assertEquals(400, response.statusCode)
    }

    @Test
    fun fight_OnePlayerNotFound() {

        // Arrange
        val player1Dto = getPlayerDto("2", "bar", 0)
        val playerSearchDto = PlayerSearchDto("2","bar")

        // found player 1
        wiremockServerPlayer.stubFor(
                WireMock.get(
                        WireMock.urlMatching(".*/players/1"))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json; charset=utf-8")
                                .withBody(player1Dto)))
        // NOT found player 2
        wiremockServerPlayer.stubFor(
                WireMock.get(
                        WireMock.urlMatching(".*/players/2"))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json; charset=utf-8")
                                .withStatus(404)))

        // Act
        val response = RestAssured
                .given()
                .auth().basic("foo", "123")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(playerSearchDto)
                .post("/startRound")

        // Assert
        Assert.assertEquals(404, response.statusCode)
    }

    @Test
    fun startRound_Success(){
        //Arrange
        val fooJson =
                """
        [
            {
                "id": "1",
                "username": "foo",
                "correctAnswers": "0"
            }
        ]
        """
        val barJson =
                """
            {
                "id": "2",
                "username": "bar",
                "correctAnswers": "0"
            }
        """
        val playersQuizRoundIdsDto = PlayerSearchDto("2","bar")
        val quizzes = getJsonQuizzes()
        // mock player 1
        wiremockServerPlayer.stubFor(
                WireMock.get(
                        WireMock.urlMatching(".*foo"))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json; charset=utf-8")
                                .withBody(fooJson)))
        // mock player 2
        wiremockServerPlayer.stubFor(
                WireMock.get(
                        WireMock.urlMatching(".*players/2"))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json; charset=utf-8")
                                .withBody(barJson)))
        //mock 4 quizzes
        wiremockServerQuiz.stubFor(
                WireMock.get(
                        WireMock.urlMatching(".*/randomQuizzes"))
                        .willReturn(WireMock.aResponse()
                                .withHeader("Content-Type", "application/json; charset=utf-8")
                                .withBody(quizzes))
        )
        // new round  created
        wiremockServerRound.stubFor(
                WireMock.post(
                        WireMock.urlMatching(".*/rounds"))
                        .willReturn(WireMock.aResponse().withStatus(201)))
        // Act
        val response = RestAssured
                .given()
                .auth().basic("foo", "123")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(playersQuizRoundIdsDto)
                .post("/startRound")

        val quizResultLogDto = response.then().extract().`as`(QuizResultLogDto::class.java)

        // Assert
        Assert.assertEquals(200, response.statusCode)
        Assert.assertTrue(quizResultLogDto.winner == "foo" || quizResultLogDto.winner == "bar")
    }
}