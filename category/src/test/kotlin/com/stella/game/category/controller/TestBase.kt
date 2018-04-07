package com.stella.game.category.controller

import com.stella.game.category.Application
import com.stella.game.schema.CategoryDto
import com.stella.game.schema.QuizDto
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.springframework.boot.context.embedded.LocalServerPort
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.logging.Level
import java.util.logging.Logger

@RunWith(SpringRunner::class)
@SpringBootTest(classes = arrayOf(Application::class),
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class TestBase {

    private val logger: Logger = Logger.getLogger(TestBase::class.java.canonicalName)

    @LocalServerPort
    protected var port = 0

    @Before
    @After
    fun clean() {

        logger.log(Level.INFO, port.toString())


        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
        RestAssured.basePath = "/categories"
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()

        val list = RestAssured.given().accept(ContentType.JSON).get()
                .then()
                .statusCode(200)
                .extract()
                .`as`(Array<CategoryDto>::class.java)
                .toList()

        list.stream().forEach {
            RestAssured.given().pathParam("id", it.id)
                    .delete("/{id}")
                    .then()
                    .statusCode(204)
        }

        RestAssured.given().get()
                .then()
                .statusCode(200)
                .body("size()", CoreMatchers.equalTo(0))
    }

    fun getCategoryDto():CategoryDto {
        return CategoryDto(null, "sports")
    }

    fun postNewCategory(dto: CategoryDto) : Long{
        return RestAssured.given().contentType(ContentType.JSON)
                .body(dto)
                .post()
                .then()
                .statusCode(201)
                .extract().`as`(Long::class.java)
    }
}