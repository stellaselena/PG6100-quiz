package com.stella.game.category.controller

import com.stella.game.schema.CategoryDto
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class MatchResultControllerTest : TestBase() {
    @Test
    fun testCleanDB() {
        RestAssured.given().get().then()
                .statusCode(200)
                .body("size()", CoreMatchers.equalTo(0))
    }

    @Test
    fun testCreateCategory(){
        // POST /matches
        var category = getCategoryDto()

        // valid dto
        val id = RestAssured.given().contentType(ContentType.JSON)
                .body(category)
                .post()
                .then()
                .statusCode(201)
                .extract().`as`(Long::class.java)
        Assert.assertNotNull(id)

        // invalid dto
        RestAssured.given().contentType(ContentType.JSON)
                .body("5318008")
                .post()
                .then()
                .statusCode(400)
    }

    @Test
    fun testGetCategory(){
        // Arrange
        val item = getCategoryDto()
        val id = postNewCategory(item)

        val response = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id",id)
                .get("/{id}")
                .then()
                .statusCode(200)
                .extract()
                .`as`(CategoryDto::class.java)
        Assert.assertTrue(item.name == response.name)

        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id","text")
                .get("/{id}")
                .then()
                .statusCode(404)

        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id",5318008)
                .get("{id}")
                .then()
                .statusCode(404)
    }

    @Test
    fun testDelete(){
        val itemDto = getCategoryDto()
        val id = postNewCategory(itemDto)

        val response = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id",id)
                .get("/{id}")
                .then()
                .statusCode(200)
                .extract()
                .`as`(CategoryDto::class.java)
        Assert.assertTrue(itemDto.name == response.name)

        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id","text")
                .delete("/{id}")
                .then()
                .statusCode(400)

        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id",id)
                .delete("/{id}")
                .then()
                .statusCode(204)


        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id",id)
                .get("{id}")
                .then()
                .statusCode(404)
    }

}