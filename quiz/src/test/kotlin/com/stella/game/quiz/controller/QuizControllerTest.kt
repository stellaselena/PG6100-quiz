package com.stella.game.quiz.controller

import com.stella.game.schema.QuizDto
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test

class QuizRepositoryImplTest : TestBase(){
    @Before
    fun assertThatDatabaseIsEmpty() {
        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(0))
    }

    @Test
    fun testCreateAndGet(){

        val answers  = mutableListOf("a", "b", "c", "d")
        val dto = QuizDto(null, "question", mutableListOf("a", "b", "c", "d"), 1, 1)

        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(0))

        val id = RestAssured.given().contentType(ContentType.JSON)
                .body(dto)
                .post()
                .then()
                .statusCode(201)
                .extract().asString()

        RestAssured.given().get().then().statusCode(200).body("size()", CoreMatchers.equalTo(1))

        RestAssured.given().pathParam("id", id)
                .get("/{id}")
                .then()
                .statusCode(200)
                .body("id", CoreMatchers.equalTo(id))
                .body("question", CoreMatchers.equalTo("question"))
                .body("correctAnswer", CoreMatchers.equalTo(1))

    }

    @Test
    fun testDelete() {
        val answers  = ArrayList(listOf("a", "b", "c", "d"))

        val id = RestAssured.given().contentType(ContentType.JSON)
                .body(QuizDto(null, "question", mutableListOf("a", "b", "c", "d"), 1, 1))
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
    fun testUpdate() {

        //first create with a POST
        val id = RestAssured.given().contentType(ContentType.JSON)
                .body(QuizDto(null, "question", mutableListOf("a", "b", "c", "d"), 1, 1))
                .post()
                .then()
                .statusCode(201)
                .extract().asString()

        val updatedQuestion = "new updated question"

        //now change text with PUT
        RestAssured.given().contentType(ContentType.JSON)
                .pathParam("id", id)
                .body(QuizDto(id,updatedQuestion, mutableListOf("a", "b", "c", "d"), 1, 1))
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
    fun testGetAll() {

        RestAssured.get().then().body("size()", CoreMatchers.equalTo(0))
        createSomeQuizzes()

        RestAssured.get().then().body("size()", CoreMatchers.equalTo(5))
    }

    private fun createSomeQuizzes() {
        val answers  = mutableListOf("a", "b", "c", "d")

        createQuiz("question1", answers, 1, 1)
        createQuiz("question2", answers, 2, 1)
        createQuiz("question3", answers, 3, 2)
        createQuiz("question4", answers, 1, 3)
        createQuiz("question5", answers, 2, 3)

    }


    private fun createQuiz(question: String, answers: MutableList<String>, correctAnswer: Int, subcategory: Long) {

        RestAssured.given().contentType(ContentType.JSON)
                .body(QuizDto(null, question, answers, correctAnswer, subcategory))
                .post()
                .then()
                .statusCode(201)
    }
}