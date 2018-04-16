package com.stella.game.subcategory.controller

import com.github.tomakehurst.wiremock.client.WireMock
import com.stella.game.schema.CategoryDto
import com.stella.game.schema.SubcategoryDto
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class SubcategoryControllerTest : WiremockTestBase() {

    fun getSubcategoryDto(): SubcategoryDto {
        wiremockServerCategory.stubFor(
                WireMock.get(WireMock.urlMatching(".*/categories/1"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)))

        val category = CategoryDto(id = "1", name = "sports")

        return SubcategoryDto(null, "tennis", category.id!!.toLong())
    }

    fun postNewSubcategory(dto: SubcategoryDto): Long {
        return RestAssured.given().contentType(ContentType.JSON)
                .body(dto)
                .post()
                .then()
                .statusCode(201)
                .extract().`as`(Long::class.java)
    }
    @Test
    fun testCleanDB() {
        RestAssured.given().get().then()
                .statusCode(200)
                .body("size()", CoreMatchers.equalTo(0))
    }

    @Test
    fun testCreateSubcategory(){
        // POST /matches
        var subcategory = getSubcategoryDto()

        // valid dto
        val id = RestAssured.given().contentType(ContentType.JSON)
                .body(subcategory)
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
    fun testGetSubcategories(){
        val sub = getSubcategoryDto()
        postNewSubcategory(sub)
        val sub2 = getSubcategoryDto()
        postNewSubcategory(sub2)

        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .get().then().statusCode(200)
                .body("size()", CoreMatchers.equalTo(2))

    }

    @Test
    fun testGetSubcategory(){
        val sub = getSubcategoryDto()
        val id = postNewSubcategory(sub)

        val response = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id",id)
                .get("/{id}")
                .then()
                .statusCode(200)
                .extract()
                .`as`(SubcategoryDto::class.java)
        Assert.assertTrue(sub.name == response.name)

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
        val subcategoryDto = getSubcategoryDto()
        val id = postNewSubcategory(subcategoryDto)

        val response = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id",id)
                .get("/{id}")
                .then()
                .statusCode(200)
                .extract()
                .`as`(SubcategoryDto::class.java)
        Assert.assertTrue(subcategoryDto.name == response.name)

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

    @Test
    fun testReplaceName(){
        // Arrange
        val subcategoryDto = getSubcategoryDto()
        val id = postNewSubcategory(subcategoryDto)

        // GET /subcategories/:id
        val response = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id",id)
                .get("/{id}")
                .then()
                .statusCode(200)
                .extract()
                .`as`(SubcategoryDto::class.java)
        Assert.assertTrue(subcategoryDto.name == response.name)

        subcategoryDto.name = "football"
        subcategoryDto.id = id.toString()

        RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id", id)
                .body(subcategoryDto)
                .put("/{id}")
                .then()
                .statusCode(204)

        val response2 = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .pathParam("id",id)
                .get("/{id}")
                .then()
                .statusCode(200)
                .extract()
                .`as`(SubcategoryDto::class.java)
        Assert.assertTrue(subcategoryDto.name == response2.name)
        Assert.assertTrue(response2.id == response.id)
    }


}