package com.stella.game.subcategory.controller

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.stella.game.schema.SubcategoryDto
import com.stella.game.subcategory.domain.model.Subcategory
import io.restassured.RestAssured
import io.restassured.http.ContentType
import org.hamcrest.CoreMatchers
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.logging.Logger

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
abstract class WiremockTestBase {

    private val logger : Logger = Logger.getLogger(WiremockTestBase::class.java.canonicalName)


    companion object {
        lateinit var wiremockServerItem: WireMockServer

        @BeforeClass
        @JvmStatic

        fun initClass() {
            RestAssured.baseURI = "http://localhost"
            RestAssured.port = 7082
            RestAssured.basePath = "/subcategories"
            RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()

            wiremockServerItem = WireMockServer(WireMockConfiguration.wireMockConfig().port(8084).notifier(ConsoleNotifier(true)))

            wiremockServerItem.start()

        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            wiremockServerItem.stop()
        }
    }

    @Before
    @After
    fun cleanDatabase() {

        val list = RestAssured.given().accept(ContentType.JSON).get()
                .then()
                .statusCode(200)
                .extract()
                .`as`(Array<Subcategory>::class.java)
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

}