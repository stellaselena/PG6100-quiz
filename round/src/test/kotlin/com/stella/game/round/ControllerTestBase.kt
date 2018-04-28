package com.stella.game.round

import com.stella.game.schema.PlayerResultDto
import com.stella.game.schema.RoundDto
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
abstract class ControllerTestBase{

    private val logger : Logger = Logger.getLogger(ControllerTestBase::class.java.canonicalName)

    @LocalServerPort
    protected var port = 0


//    @Value("\${server.contextPath}")
//    private lateinit var contextPath : String

    @Before
    @After
    fun clean() {

        logger.log(Level.INFO, port.toString())

        RestAssured.baseURI = "http://localhost"
        RestAssured.port = port
        RestAssured.basePath = "/rounds"
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()

        val list = RestAssured.given().accept(ContentType.JSON).get()
                .then()
                .statusCode(200)
                .extract()
                .`as`(Array<RoundDto>::class.java)
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

    fun getValidRoundDto():RoundDto {
        val player1Winner = PlayerResultDto("1","player1", 4)
        val player2  = PlayerResultDto("2","player2", 2)
        return RoundDto(player1Winner, player2, player1Winner.username)
    }

    fun postNewRoundValid(dto: RoundDto) : Long{
        return RestAssured.given().contentType(ContentType.JSON)
                .body(dto)
                .post()
                .then()
                .statusCode(201)
                .extract().`as`(Long::class.java)
    }
}