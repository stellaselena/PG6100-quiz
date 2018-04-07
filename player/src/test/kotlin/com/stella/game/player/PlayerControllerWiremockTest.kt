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

        wiremockServerItem.stubFor(
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

        // Stub 200, item with 1 should exist
        wiremockServerItem.stubFor(
                WireMock.get(WireMock.urlMatching(".*/quizzes/1"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)))

        // Try to add item to non-existant user.
        val responseInvalidUserId = RestAssured.given()
                .contentType(ContentType.JSON)
                .body(quiz)
                .post("/4/quizzes")
                .then()
                .statusCode(404)


        // Add item with id that does not exist
        val itemWithInvalidId = QuizDto(id = "2")
        wiremockServerItem.stubFor(
                WireMock.get(WireMock.urlMatching(".*/quizzes/2"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(404)))
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(itemWithInvalidId)
                .post("/$savedId/quizzes")
                .then()
                .statusCode(404)

        // Try to add item without an id
        val itemWithoutId = QuizDto(id = "")
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(itemWithoutId)
                .post("/$savedId/quizzes")
                .then()
                .statusCode(404)

        // Try to add item with text as an id
        val itemWithTextId = QuizDto(id = "hello")
        RestAssured.given()
                .contentType(ContentType.JSON)
                .body(itemWithTextId)
                .post("/$savedId/quizzes")
                .then()
                .statusCode(404)

        // Try to add same item twice
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