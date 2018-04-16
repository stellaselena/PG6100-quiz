package com.stella.game.player

import com.github.tomakehurst.wiremock.client.WireMock
import io.restassured.RestAssured
import io.restassured.http.ContentType
import junit.framework.Assert
import org.junit.Test
import com.stella.game.schema.PlayerDto
import com.stella.game.schema.QuizDto

class PlayerControllerWiremockTest : WiremockTestBase() {

    @Test
    fun testAddQuizToPlayer_Valid() {

        val playerDto1 = PlayerDto(
                "Stella",
                null,
                listOf()
        )
        val savedId = RestAssured.given().contentType(ContentType.JSON)
                .body(playerDto1)
                .post()
                .then()
                .statusCode(201)
                .extract().`as`(Long::class.java)

        wiremockServerQuiz.stubFor(
                WireMock.get(WireMock.urlMatching(".*/quizzes/1"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)))

        val quiz = QuizDto(id = "1")

        val response = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(quiz)
                .post("/$savedId/quizzes")

        print(response.print())
        Assert.assertEquals(200, response.statusCode)
    }

    @Test
    fun testAddQuizToPlayer_Invalid() {
        val playerDto1 = PlayerDto(
                "Stefan",
                null,
                listOf(4L)
        )
        val quiz = QuizDto(id = "1")
        val savedId = RestAssured.given().contentType(ContentType.JSON)
                .body(playerDto1)
                .post()
                .then()
                .statusCode(201)
                .extract().`as`(Long::class.java)

        // Stub 200, quiz with 1 should exist
        wiremockServerQuiz.stubFor(
                WireMock.get(WireMock.urlMatching(".*/quizzes/1"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)))

        // Try to add quiz to non-existant user.
        val responseInvalidUserId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(quiz)
                .post("/4/quizzes")
                .then()
                .statusCode(404)


        // Add quiz with id that does not exist
        val quizWithInvalidId = QuizDto(id = "2")
        wiremockServerQuiz.stubFor(
                WireMock.get(WireMock.urlMatching(".*/quizzes/2"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(404)))
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(quizWithInvalidId)
                .post("/$savedId/quizzes")
                .then()
                .statusCode(404)

        // Try to add quiz without an id
        val quizWithoutId = QuizDto(id = "")
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(quizWithoutId)
                .post("/$savedId/quizzes")
                .then()
                .statusCode(404)

        // Try to add quiz with text as an id
        val quizWithTextId = QuizDto(id = "hello")
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(quizWithTextId)
                .post("/$savedId/quizzes")
                .then()
                .statusCode(404)

        // Try to add same quiz twice
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(quiz)
                .post("/$savedId/quizzes")
                .then()
                .statusCode(200)
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(quiz)
                .post("/$savedId/quizzes")
                .then()
                .statusCode(400)
    }
}