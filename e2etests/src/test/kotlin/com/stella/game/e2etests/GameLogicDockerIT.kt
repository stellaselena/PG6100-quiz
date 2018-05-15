package com.stella.game.e2etests

import com.stella.game.schema.CategoryDto
import com.stella.game.schema.QuizDto
import com.stella.game.schema.SubcategoryDto
import org.testcontainers.containers.DockerComposeContainer
import io.restassured.RestAssured
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import com.stella.game.schema.gamelogic.PlayerSearchDto
import org.awaitility.Awaitility.await
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.Matchers.contains
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Test
import java.io.File
import java.util.concurrent.TimeUnit

class GameLogicDockerIT {
    companion object {

        class KDockerComposeContainer(path: File) : DockerComposeContainer<KDockerComposeContainer>(path)


        @ClassRule
        @JvmField
        val env = KDockerComposeContainer(File("../docker-compose.yml"))
                .withLocalCompose(true)

        private var counter = System.currentTimeMillis()

        @BeforeClass
        @JvmStatic
        fun initialize() {
            RestAssured.baseURI = "http://localhost"
            RestAssured.port = 10000
            RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()


            await().atMost(240, TimeUnit.SECONDS)
                    .ignoreExceptions()
                    .until({
                        // check GATEWAY is available
                        given().get("http://localhost:10000/api/v1/user").then().statusCode(401)

                        true
                    })
        }
    }


    @Test
    fun testUnauthorizedAccess() {
        RestAssured.given().get("/api/v1/gamelogic-server/play/opponent")
                .then()
                .statusCode(401)

        RestAssured.given().get("/api/v1/gamelogic-server/play/startRound")
                .then()
                .statusCode(401)
    }

    @Test
    fun findOpponentAndStartRound() {
        // Arrange
        val id1 = createUniqueId()
        val cookie1 = registerUser(id1, "password")

        await().atMost(120, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until({
                    RestAssured.given()
                            .cookie("SESSION", cookie1.session)
                            .get("/api/v1/gamelogic-server/play/opponent")
                            .then()
                            .statusCode(404)
                    true
                })

        val id2 = createUniqueId()
        val cookie2 = registerUser(id2, "password")

        val responseFindOpponent = RestAssured.given()
                .cookie("SESSION", cookie2.session)
                .accept(ContentType.JSON)
                .get("/api/v1/gamelogic-server/play/opponent")
        assertEquals(200, responseFindOpponent.statusCode)

        println(responseFindOpponent.body.print())
        val playerSearchDto = responseFindOpponent.`as`(PlayerSearchDto::class.java)

       //todo add quizzes
        val category = CategoryDto(null, "sports")
        val categoryReturned = RestAssured.given().contentType(ContentType.JSON)
                .cookie("SESSION", cookie2.session)
                .header("X-XSRF-TOKEN", cookie2.csrf)
                .cookie("XSRF-TOKEN", cookie2.csrf)
                .body(category)
                .post("/api/v1/category-server/categories")
                .then()
                .statusCode(201)

        val subcategory = SubcategoryDto(null, "tennis", 1.toLong())
        val subcategoryReturned = RestAssured.given().contentType(ContentType.JSON)
                .cookie("SESSION", cookie2.session)
                .header("X-XSRF-TOKEN", cookie2.csrf)
                .cookie("XSRF-TOKEN", cookie2.csrf)
                .body(subcategory)
                .post("/api/v1/subcategory-server/subcategories")
                .then()
                .statusCode(201)

        val quiz = QuizDto(null, "question", mutableListOf("a", "b", "c", "d"), 1, 1.toLong())
        val quiz2 = QuizDto(null, "question", mutableListOf("a", "b", "c", "d"), 1, 1.toLong())
        val quizReturned1 = RestAssured.given().contentType(ContentType.JSON)
                .cookie("SESSION", cookie2.session)
                .header("X-XSRF-TOKEN", cookie2.csrf)
                .cookie("XSRF-TOKEN", cookie2.csrf)
                .body(quiz)
                .post("/api/v1/quiz-server/quizzes")
                .then()
                .statusCode(201)
        val quizReturned2 = RestAssured.given().contentType(ContentType.JSON)
                .cookie("SESSION", cookie2.session)
                .header("X-XSRF-TOKEN", cookie2.csrf)
                .cookie("XSRF-TOKEN", cookie2.csrf)
                .body(quiz2)
                .post("/api/v1/quiz-server/quizzes")
                .then()
                .statusCode(201)

        val responseRound = RestAssured.given().contentType(ContentType.JSON)
                .cookie("SESSION", cookie2.session)
                .header("X-XSRF-TOKEN", cookie2.csrf)
                .cookie("XSRF-TOKEN", cookie2.csrf)
                .body(playerSearchDto)
                .post("/api/v1/gamelogic-server/play/startRound")
        assertTrue(responseRound.body.print().contains(id2))

        assertEquals(200, responseRound.statusCode)


        // round persisted (checking rabbitMQ)
        await().atMost(20, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until({
                    RestAssured.given().cookie("SESSION", cookie2.session)
                            .get("/api/v1/round-server/rounds")
                            .then()
                            .statusCode(200)
                            .and()
                    true
                })
    }

    private fun createUniqueId(): String {
        counter++
        return "foo_${counter}"
    }

    class NeededCookies(val session:String, val csrf: String)

    private fun registerUser(id: String, password: String): NeededCookies {

        val xsrfToken = RestAssured.given().contentType(ContentType.URLENC)
                .formParam("the_user", id)
                .formParam("the_password", password)
                .post("/api/v1/signIn")
                .then()
                .statusCode(403)
                .extract().cookie("XSRF-TOKEN")

        val session =  RestAssured.given().contentType(ContentType.URLENC)
                .formParam("the_user", id)
                .formParam("the_password", password)
                .header("X-XSRF-TOKEN", xsrfToken)
                .cookie("XSRF-TOKEN", xsrfToken)
                .post("/api/v1/signIn")
                .then()
                .statusCode(204)
                .extract().cookie("SESSION")

        return NeededCookies(session, xsrfToken)
    }


}