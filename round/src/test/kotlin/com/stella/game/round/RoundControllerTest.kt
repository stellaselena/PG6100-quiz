package com.stella.game.round

import com.stella.game.schema.PlayerResultDto
import com.stella.game.schema.RoundDto
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class RoundControllerTest : ControllerTestBase() {
    @Test
    fun testCleanDB() {
        RestAssured.given().get().then()
                .statusCode(200)
                .body("size()", CoreMatchers.equalTo(0))
    }

    @Test
    fun testCreateRound() {
        var dto = getValidRoundDto()

        // valid dto
        val id = RestAssured.given().contentType(ContentType.JSON)
                .body(dto)
                .post()
                .then()
                .statusCode(201)
                .extract().`as`(Long::class.java)
        Assert.assertNotNull(id)

        // invalid dto
        RestAssured.given().contentType(ContentType.JSON)
                .body("dasd")
                .post()
                .then()
                .statusCode(400)

    }

    @Test
    fun testGetRounds() {
        //Arrange
        val dto = getValidRoundDto()
        val id = postNewRoundValid(dto)

        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get().then().statusCode(200)
                .body("size()", CoreMatchers.equalTo(1))

        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .param("username", dto.player1!!.username)
                .get().then().statusCode(200)
                .body("size()", CoreMatchers.equalTo(1))

        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .param("username", "nonexist")
                .get().then()
                .statusCode(200).body("size()", CoreMatchers.equalTo(0))
    }

    @Test
    fun testGetRound() {
        // Arrange
        val dto = getValidRoundDto()
        val id = postNewRoundValid(dto)

        val dtoRespone1 = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id", id)
                .get("/{id}")
                .then()
                .statusCode(200)
                .extract()
                .`as`(RoundDto::class.java)
        Assert.assertTrue(dto.player1!!.username == dtoRespone1.player1!!.username)

        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id", "invalid_input")
                .get("/{id}")
                .then()
                .statusCode(400)


        val notExistRoundId = 555
        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id", notExistRoundId)
                .get("{id}")
                .then()
                .statusCode(404)
    }

    @Test
    fun testDeleteRound() {
        // Arrange
        val dto = getValidRoundDto()
        val id = postNewRoundValid(dto)

        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id", id)
                .delete("/{id}")
                .then()
                .statusCode(204)

        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id", "invalid_input")
                .delete("/{id}")
                .then()
                .statusCode(400)

        val notExistRoundId = 555
        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id", notExistRoundId)
                .delete("{id}")
                .then()
                .statusCode(404)
    }

    @Test
    fun testUpdateRound_Success() {

        val id = postNewRoundValid(getValidRoundDto())
        val dto2 = RoundDto(
                PlayerResultDto("1", "player1", 4),
                PlayerResultDto("2", "player3", 2),
                "player3", id.toString())


        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id", id)
                .body(dto2)
                .put("/{id}")
                .then()
                .statusCode(204)

        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id", id)
                .get("/{id}")
                .then()
                .statusCode(200)
                .body("player1.username", CoreMatchers.equalTo("player1"))
                .body("player2.username", CoreMatchers.equalTo("player3"))
    }

    @Test
    fun testUpdateWinnerName() {
        // Arrange
        val dto = RoundDto(
                PlayerResultDto("1", "player1", 4),
                PlayerResultDto("2", "player2", 2),
                "player1")
        val id = postNewRoundValid(dto)
        val newWinnerName = "player2"

        Assert.assertNotEquals(dto.winnerName, newWinnerName)

        RestAssured.given()
                .pathParam("id", "wrong")
                .body(newWinnerName)
                .patch("/{id}")
                .then()
                .statusCode(400)

        RestAssured.given()
                .pathParam("id", 123123123)
                .body(newWinnerName)
                .patch("/{id}")
                .then()
                .statusCode(404)

        RestAssured.given()
                .pathParam("id", id)
                .body("")
                .patch("/{id}")
                .then()
                .statusCode(400)

        // valid
        RestAssured.given()
                .pathParam("id", id)
                .body(newWinnerName)
                .patch("/{id}")
                .then()
                .statusCode(204)

        // Assert
        val dtoResponse = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id", id)
                .get("/{id}")
                .then()
                .statusCode(200)
                .extract()
                .`as`(RoundDto::class.java)

        Assert.assertEquals(newWinnerName, dtoResponse.winnerName)

    }
}