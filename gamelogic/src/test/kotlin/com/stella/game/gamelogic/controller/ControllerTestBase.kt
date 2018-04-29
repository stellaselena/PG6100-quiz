package com.stella.game.gamelogic.controller

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.common.ConsoleNotifier
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import io.restassured.RestAssured
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.util.logging.Logger

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
abstract class ControllerTestBase {

    private val logger: Logger = Logger.getLogger(ControllerTestBase::class.java.canonicalName)

    companion object {
        lateinit var wiremockServerRound: WireMockServer
        lateinit var wiremockServerPlayer: WireMockServer
        lateinit var wiremockServerQuiz: WireMockServer

        @BeforeClass
        @JvmStatic
        fun initClass() {
            RestAssured.baseURI = "http://localhost"
            RestAssured.port = 9086
            RestAssured.basePath = "/play"
            RestAssured.enableLoggingOfRequestAndResponseIfValidationFails()


            wiremockServerRound = WireMockServer(WireMockConfiguration.wireMockConfig().port(8085).notifier(ConsoleNotifier(true)))
            wiremockServerPlayer = WireMockServer(WireMockConfiguration.wireMockConfig().port(8081).notifier(ConsoleNotifier(true)))
            wiremockServerQuiz = WireMockServer(WireMockConfiguration.wireMockConfig().port(8083).notifier(ConsoleNotifier(true)))


            wiremockServerRound.start()
            wiremockServerPlayer.start()
            wiremockServerQuiz.start()
        }

        @AfterClass
        @JvmStatic
        fun tearDown() {
            wiremockServerRound.stop()
            wiremockServerPlayer.stop()
            wiremockServerQuiz.stop()
        }
    }

    fun getMockedJson_FooByUsername(): String {
        var json = """
        [
            {
                "id": "1",
                "username": "foo"
            }
        ]
        """
        return json
    }

    fun getMockedJson_FooAndBar(): String {
        var json = """
        [
            {
                "id": "1",
                "username": "foo"
            },
            {
                "id": "2",
                "username": "bar"
            }
        ]
        """
        return json
    }

    fun getMockedJson_BarById(): String {
        var json = """

            {
                "id": "2",
                "username": "bar"
            }

        """
        return json
    }

    fun getMockedJson_EmptyArray(): String {
        var json = "[]"
        return json
    }

    // only for tests
    fun getPlayerDto(id: String, username: String, correctAnswers: Int?): String {
        var json = """
        {
            "userId": "$id",
            "username": "$username"
            "correctAnswers": "$correctAnswers"
        }
        """
        return json
    }

    fun getEmptyObject(): String {
        var json = "{}"
        return json
    }

    fun getJsonQuiz(): String {
        var json = """
            [
                {
                    "id": "1",
                    "question": "Test",
                    "answers": "["a", "b", "c", "d"]",
                    "correctAnswer": "1"
                }
            ]
            """
        return json
    }

    fun getJsonQuizzes(): String {
        var json = """
            [
                {
                    "id": "1",
                    "question": "Test",
                    "answers": ["a", "b", "c", "d"],
                    "correctAnswer": "1"
                },
                {
                    "id": "2",
                    "question": "Test",
                    "answers": ["a", "b", "c", "d"],
                    "correctAnswer": "1"
                },
                  {
                    "id": "3",
                    "question": "Test",
                    "answers": ["a", "b", "c", "d"],
                    "correctAnswer": "1"
                },
                  {
                    "id": "4",
                    "question": "Test",
                    "answers": ["a", "b", "c", "d"],
                    "correctAnswer": "1"
                }
            ]
            """
        return json
    }

}