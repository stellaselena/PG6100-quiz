package com.stella.game.e2etests

import com.stella.game.schema.CategoryDto
import com.stella.game.schema.QuizDto
import com.stella.game.schema.SubcategoryDto
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.awaitility.Awaitility
import org.awaitility.Awaitility.await
import org.hamcrest.CoreMatchers
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Test
import org.testcontainers.containers.DockerComposeContainer
import java.io.File
import java.util.concurrent.TimeUnit

class BaseModulesIT {

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


            Awaitility.await().atMost(400, TimeUnit.SECONDS)
                    .ignoreExceptions()
                    .until({
                        RestAssured.given().get("http://localhost:10000/api/v1/user").then().statusCode(401)

                        true
                    })
        }
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

    private fun createUniqueId(): String {
        counter++
        return "foo_${counter}"
    }

    @Test
    fun testAccessToBaseModulesAndRabbitMq() {
        val id = createUniqueId()
        val pwd = "bar"

        val cookies = registerUser(id, pwd)

        RestAssured.given().get("/api/v1/player-server/players").then().statusCode(401)

        RestAssured.given().cookie("SESSION", cookies.session)
                .get("/api/v1/player-server/players")
                .then()
                .statusCode(200)


        RestAssured.given().get("/api/v1/category-server/categories").then().statusCode(401)

        RestAssured.given().cookie("SESSION", cookies.session)
                .get("/api/v1/category-server/categories")
                .then()
                .statusCode(200)


        val category = CategoryDto(null, "sports")
        val categoryReturned = RestAssured.given().contentType(ContentType.JSON)
                .cookie("SESSION", cookies.session)
                .header("X-XSRF-TOKEN", cookies.csrf)
                .cookie("XSRF-TOKEN", cookies.csrf)
                .body(category)
                .post("/api/v1/category-server/categories")
                .then()
                .statusCode(201)


        RestAssured.given().get("/api/v1/subcategory-server/subcategories").then().statusCode(401)

        RestAssured.given().cookie("SESSION", cookies.session)
                .get("/api/v1/subcategory-server/subcategories")
                .then()
                .statusCode(200)


        val subcategory = SubcategoryDto(null, "tennis", 1.toLong())
        val subcategoryReturned = RestAssured.given().contentType(ContentType.JSON)
                .cookie("SESSION", cookies.session)
                .header("X-XSRF-TOKEN", cookies.csrf)
                .cookie("XSRF-TOKEN", cookies.csrf)
                .body(subcategory)
                .post("/api/v1/subcategory-server/subcategories")
                .then()
                .statusCode(201)

        println(subcategoryReturned)


        RestAssured.given().get("/api/v1/quiz-server/quizzes").then().statusCode(401)

        RestAssured.given().cookie("SESSION", cookies.session)
                .get("/api/v1/quiz-server/quizzes")
                .then()
                .statusCode(200)

        val quiz = QuizDto(null, "question", mutableListOf("a", "b", "c", "d"), 1, 1.toLong())
        val quizReturned = RestAssured.given().contentType(ContentType.JSON)
                .cookie("SESSION", cookies.session)
                .header("X-XSRF-TOKEN", cookies.csrf)
                .cookie("XSRF-TOKEN", cookies.csrf)
                .body(quiz)
                .post("/api/v1/quiz-server/quizzes")
                .then()
                .statusCode(201)

        await().atMost(10, TimeUnit.SECONDS)
                .ignoreExceptions()
                .until({
                    RestAssured.given().cookie("SESSION", cookies.session)
                            .get("/api/v1/player-server/players")
                            .then()
                            .statusCode(200)
                            .and()
                            .body("username", CoreMatchers.hasItem(id))
                    true
                })
    }
}