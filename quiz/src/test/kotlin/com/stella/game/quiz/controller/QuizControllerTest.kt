package com.stella.game.quiz.controller

import com.github.tomakehurst.wiremock.client.WireMock
import com.stella.game.schema.QuizDto
import com.stella.game.schema.SubcategoryDto
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test

class QuizRepositoryImplTest : WiremockTestBase(){
    @Before
    fun assertThatDatabaseIsEmpty() {
        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(0))
    }

    @Test
    fun testCreateAndGet(){


        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(0))

        wiremockServerItem.stubFor(
                WireMock.get(WireMock.urlMatching(".*/subcategories/1"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)))

        val subcategory = SubcategoryDto(id= 1.toString(), category = 1)

        val dto = QuizDto(null, "question", mutableListOf("a", "b", "c", "d"), 1, subcategory.id?.toLong())


        val id = RestAssured.given().contentType(ContentType.JSON)
                .body(dto)
                .post()
                .then()
                .statusCode(201)
                .extract().asString()


        RestAssured.given().pathParam("id", id)
                .get("/{id}")
                .then()
                .statusCode(200)
                .body("id", CoreMatchers.equalTo(id))
                .body("question", CoreMatchers.equalTo("question"))
                .body("correctAnswer", CoreMatchers.equalTo(1))

    }

    @Test
    fun testUpdate() {

        wiremockServerItem.stubFor(
                WireMock.get(WireMock.urlMatching(".*/subcategories/1"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)))

        val subcategory = SubcategoryDto(id= 1.toString(), category = 1)

        val dto = QuizDto(null, "question", mutableListOf("a", "b", "c", "d"), 1, subcategory.id?.toLong())


        //first create with a POST
        val id = RestAssured.given().contentType(ContentType.JSON)
                .body(dto)
                .post()
                .then()
                .statusCode(201)
                .extract().asString()

        val updatedQuestion = "new updated question"

        //now change question with PUT
        RestAssured.given().contentType(ContentType.JSON)
                .pathParam("id", id)
                .body(QuizDto(id,updatedQuestion, mutableListOf("a", "b", "c", "d"), 1, subcategory.id?.toLong()))
                .put("/{id}")
                .then()
                .statusCode(204)

        RestAssured.given().pathParam("id", id)
                .get("/{id}")
                .then()
                .statusCode(200)
                .body("id", CoreMatchers.equalTo(id))
                .body("question", CoreMatchers.equalTo("new updated question"))
                .body("correctAnswer", CoreMatchers.equalTo(1))
    }

    @Test
    fun testDelete() {

        wiremockServerItem.stubFor(
                WireMock.get(WireMock.urlMatching(".*/subcategories/1"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)))

        val subcategory = SubcategoryDto(id= 1.toString(), category = 1)

        val dto = QuizDto(null, "question", mutableListOf("a", "b", "c", "d"), 1, subcategory.id?.toLong())

        val id = RestAssured.given().contentType(ContentType.JSON)
                .body(dto)
                .post()
                .then()
                .statusCode(201)
                .extract().asString()

        RestAssured.get().then()
                .body("size()", CoreMatchers.equalTo(1))
                .body("id[0]", CoreMatchers.containsString(id))

        RestAssured.delete(id)

        RestAssured.get().then().body("id", CoreMatchers.not(CoreMatchers.containsString(id)))
    }



    @Test
    fun testGetAll() {

        RestAssured.get().then().body("size()", CoreMatchers.equalTo(0))
        createSomeQuizzes()

        RestAssured.get().then().body("size()", CoreMatchers.equalTo(5))
    }

    private fun createSomeQuizzes() {
        val answers  = mutableListOf("a", "b", "c", "d")
        wiremockServerItem.stubFor(
                WireMock.get(WireMock.urlMatching(".*/subcategories/1"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)))

        val subcategory = SubcategoryDto(id= 1.toString(), category = 1)
        wiremockServerItem.stubFor(
                WireMock.get(WireMock.urlMatching(".*/subcategories/2"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)))

        val subcategory2 = SubcategoryDto(id= 2.toString(), category = 1)

        wiremockServerItem.stubFor(
                WireMock.get(WireMock.urlMatching(".*/subcategories/3"))
                        .willReturn(
                                WireMock.aResponse()
                                        .withStatus(200)))
        val subcategory3 = SubcategoryDto(id= 3.toString(), category = 1)
        createQuiz("question1", answers, 1, subcategory.id!!.toLong())
        createQuiz("question2", answers, 2, subcategory.id!!.toLong())
        createQuiz("question3", answers, 3, subcategory2.id!!.toLong())
        createQuiz("question4", answers, 1, subcategory3.id!!.toLong())
        createQuiz("question5", answers, 2, subcategory3.id!!.toLong())

    }


    private fun createQuiz(question: String, answers: MutableList<String>, correctAnswer: Int, subcategory: Long) {

        RestAssured.given().contentType(ContentType.JSON)
                .body(QuizDto(null, question, answers, correctAnswer, subcategory))
                .post()
                .then()
                .statusCode(201)
    }
}