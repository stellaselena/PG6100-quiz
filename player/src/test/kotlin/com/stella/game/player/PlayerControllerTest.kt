package com.stella.game.player

import com.stella.game.schema.PlayerDto
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class PlayerControllerTest : TestBase() {

    @Before
    fun assertThatDatabaseIsEmpty() {
        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(0))
    }

    @Test
    fun createAndGetPlayer_Valid() {
        val playerDto1 = getValidPlayerDtos()[0]

        val savedId = RestAssured.given().contentType(ContentType.JSON)
                .body(playerDto1)
                .post()
                .then()
                .statusCode(201)
                .extract().`as`(Long::class.java)
        val playerDto2 = getValidPlayerDtos()[1]
        val savedId2 = RestAssured.given().contentType(ContentType.JSON)
                .body(playerDto2)
                .post()
                .then()
                .statusCode(201)
                .extract().`as`(Long::class.java)

        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(2))

        val foundPlayer1 = RestAssured.given().contentType(ContentType.JSON)
                .pathParam("id", savedId)
                .get("/{id}")
                .then()
                .statusCode(200)
                .extract()
                .`as`(PlayerDto::class.java)

        Assert.assertEquals(playerDto1.username, foundPlayer1.username)
    }

    @Test
    fun createAndGetPlayer_Invalid() {
        val playerDto1 = getValidPlayerDtos()[0]
        playerDto1.username = null

        val savedId = RestAssured.given().contentType(ContentType.JSON)
                .body(playerDto1)
                .post()
                .then()
                .statusCode(400)

        // Check that nothing was saved
        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(0))
    }

    @Test
    fun getPlayerByUsername() {
        val playerDto1 = getValidPlayerDtos()[0]
        postPlayerDto(playerDto1, 201)

        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(1))


        // Check username that doesnt exist
        val firstResult = RestAssured.given().contentType(ContentType.JSON)
                .param("username", playerDto1.username?.repeat(10))
                .get()
                .then()
                .statusCode(200)
                .extract()
                .`as`(Array<PlayerDto>::class.java)
        Assert.assertEquals(0, firstResult.count())

        val secondResult = RestAssured.given().contentType(ContentType.JSON)
                .param("username", playerDto1.username)
                .get()
                .then()
                .statusCode(200)
                .extract()
                .`as`(Array<PlayerDto>::class.java)

        Assert.assertEquals(1, secondResult.count())
        Assert.assertEquals(playerDto1.username, secondResult[0].username)
    }

    @Test
    fun updatePlayer() {
        val playerDto1 = getValidPlayerDtos()[0]
        val playerDto2 = getValidPlayerDtos()[1]

        val savedId = RestAssured.given().contentType(ContentType.JSON)
                .body(playerDto1)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .`as`(Long::class.java)

        // Set id to match id after creating player.
        playerDto1.id = savedId.toString()
        playerDto2.id = savedId.toString()
        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(1))


        // Update data to be like playerDto2
        RestAssured.given().pathParam("id", savedId)
                .contentType(ContentType.JSON)
                .body(playerDto2)
                .put("/{id}")
                .then()
                .statusCode(204)

        // Validate that it changed
        RestAssured.given().pathParam("id", savedId)
                .get("/{id}")
                .then()
                .statusCode(200)
                .body("username", CoreMatchers.equalTo(playerDto2.username))

        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(1))
    }


    @Test
    fun updateUsername() {
        val playerDto = getValidPlayerDtos()[0]

        val savedId = RestAssured.given().contentType(ContentType.JSON)
                .body(playerDto)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .`as`(Long::class.java)

        // Empty body
        RestAssured.given().contentType(ContentType.TEXT)
                .pathParam("id", savedId)
                .body(" ")
                .patch("/{id}")
                .then()
                .statusCode(400)

        // Verify that it did not change
        RestAssured.given().pathParam("id", savedId)
                .get("/{id}")
                .then()
                .statusCode(200)
                .body("username", CoreMatchers.equalTo(playerDto.username))


    }

    @Test
    fun deletePlayer() {
        val playerDto = getValidPlayerDtos()[0]
        val savedId = RestAssured.given().contentType(ContentType.JSON)
                .body(playerDto)
                .post()
                .then()
                .statusCode(201)
                .extract()
                .`as`(Long::class.java)
        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(1))

        RestAssured.given().contentType(ContentType.JSON)
                .pathParam("id", savedId)
                .delete("/{id}")
                .then()
                .statusCode(204)
        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(0))
    }

    private fun postPlayerDto(playerDto: PlayerDto, expectedStatusCode: Int) {
        RestAssured.given().contentType(ContentType.JSON)
                .body(playerDto)
                .post()
                .then()
                .statusCode(expectedStatusCode)
    }

    private fun getValidPlayerDtos(): List<PlayerDto> {
        return listOf(
                PlayerDto(
                        "stella",
                        null,
                        0

                        ),
                PlayerDto(
                        "stefan",
                        null,
                        0
                ),
                PlayerDto(
                        "athena",
                        null,
                        0
                )
        )
    }

}