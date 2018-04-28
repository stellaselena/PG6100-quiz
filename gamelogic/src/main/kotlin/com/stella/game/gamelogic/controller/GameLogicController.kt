package com.stella.game.gamelogic.controller

import com.stella.game.gamelogic.domain.converters.*
import com.stella.game.gamelogic.domain.model.Participant
import com.stella.game.gamelogic.domain.model.Question
import com.stella.game.gamelogic.services.AmqpService
import com.stella.game.gamelogic.services.QuizMockService
import com.stella.game.schema.PlayerDto
import com.stella.game.schema.PlayerResultDto
import com.stella.game.schema.QuizDto
import com.stella.game.schema.RoundDto
import com.stella.game.schema.gamelogic.PlayerSearchDto
import com.stella.game.schema.gamelogic.QuizResultLogDto
import io.swagger.annotations.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.lang.Exception
import java.util.*

@Api(value = "play", description = "API for game logic")
@RequestMapping(path = arrayOf("/play"))
@RestController
@Validated
class GameLogicController {
    @Autowired
    lateinit var restTemplate: RestTemplate

    @Autowired
    lateinit var gameService: QuizMockService

    @Autowired
    lateinit var amqpService: AmqpService

    @Value("\${playerServerName}")
    private lateinit var playersPath: String

    @Value("\${quizServerName}")
    private lateinit var quizPath: String

    @ApiOperation("Find random opponent")
    @ApiResponses(
            ApiResponse(code = 200, message = "The opponent found"),
            ApiResponse(code = 404, message = "No opponent found")
    )
    @GetMapping(path = arrayOf("/opponent"), produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    fun findEnemy(authentication: Authentication): ResponseEntity<PlayerSearchDto>? {

        // 1 make request to player module.
        val response: ResponseEntity<Array<PlayerDto>> = try {
            val url = "$playersPath/players"

            restTemplate.getForEntity(url, Array<PlayerDto>::class.java)
        } catch (e: HttpClientErrorException) {
            return ResponseEntity.status(e.statusCode.value()).build()
        }

        // 3 get list. If list is empty return bad request
        val players = response.body.asList()
        if (players.isEmpty()) {
            return ResponseEntity.status(404).build()
        }


        val callerUsername = authentication.name
        val playersFiltered = excludeFromListByUsername(players, callerUsername)


        // 4 get random from list
        if (playersFiltered.isNotEmpty()) {
            return ResponseEntity.ok(PlayerSearchConverter.transform(playersFiltered[Random().nextInt(playersFiltered.size)]))
        } else {
            return ResponseEntity.status(404).build()
        }
    }

    @ApiOperation("Begin round")
    @PostMapping(path = arrayOf("/startRound"), consumes = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    @ApiResponses(
            ApiResponse(code = 200, message = "The round is over and returns log, represented as QuizResultLogDto"),
            ApiResponse(code = 404, message = "Opponent(s) not found"),
            ApiResponse(code = 400, message = "Given payload is invalid, check request body")
    )
    fun startFight(
            authentication: Authentication,
            @ApiParam("Model represent ids of users for the given round")
            @RequestBody playerSearchDto: PlayerSearchDto
    ): ResponseEntity<QuizResultLogDto> {

        if (!isPlayersQuizRoundValid(playerSearchDto)) {

            return ResponseEntity.status(400).build()
        }
        /**fetch players**/

        val callerUsername = authentication.name
        var player1: PlayerDto
        var player2: PlayerDto
        try {
            val urlPlayer1 = "$playersPath/players?username=${authentication.name}"
            val urlPlayer2 = "$playersPath/players/${playerSearchDto.id!!.toLong()}"

            val responsePlayer1: ResponseEntity<Array<PlayerDto>> = restTemplate.getForEntity(urlPlayer1, Array<PlayerDto>::class.java)
            if (responsePlayer1.body.toList().size != 1) {

                return ResponseEntity.status(400).build()
            } else {
                player1 = responsePlayer1.body.toList().first()
            }

            val responsePlayer2: ResponseEntity<PlayerDto> = restTemplate.getForEntity(urlPlayer2, PlayerDto::class.java)

            if (responsePlayer1.statusCodeValue != 200 || responsePlayer2.statusCodeValue != 200) {

                return ResponseEntity.status(404).build()
            }

            player2 = responsePlayer2.body
            if (callerUsername.toLowerCase() != (player1.username!!.toLowerCase())) {

                return ResponseEntity.status(400).build()
            }

            if (callerUsername.toLowerCase() == player2.username!!.toLowerCase()) {
                return ResponseEntity.status(400).build()
            }

        } catch (e: HttpClientErrorException) {
            return ResponseEntity.status(e.rawStatusCode).build()
        }
        var quizzes: List<QuizDto> = listOf()

        /**fetch quizzes**/
        try {
            val quizUrl = "$quizPath/quizzes/randomQuizzes"
            val responseQuiz = restTemplate.getForEntity(quizUrl, Array<QuizDto>::class.java)
            quizzes = responseQuiz.body.toList()
        } catch (e: HttpClientErrorException) {
            return ResponseEntity.status(e.rawStatusCode).build()
        }
        /**begin round**/
        val p1: Participant = PlayerQuizRoundConverter.transform(player1)
        val p2: Participant = PlayerQuizRoundConverter.transform(player2)
        val questions: List<Question> = QuizForRoundConverter.transform(quizzes)
        val quizResultGameLog = gameService.startRound(p1, p2, questions)

        val roundResult = getRound(p1, p2, quizResultGameLog.winner!!)
        try{
            amqpService.sendRoundCreated(roundResult)
        } catch (e: Exception){}

        return ResponseEntity.ok(quizResultGameLog)
    }
}

//todo remove quizzes from player entity


private fun isPlayersQuizRoundValid(dto: PlayerSearchDto): Boolean {
    try {
        val id = dto.id!!.toLong()
        return true
    } catch (e: Exception) {
    }
    return false
}


private fun getRound(player1: Participant, player2: Participant, winner: String): RoundDto {
    return RoundDto(
            PlayerResultDto(
                    player1.playerId,
                    player1.username,
                    player1.correctAnswers
            ),
            PlayerResultDto(
                    player2.playerId,
                    player2.username,
                    player2.correctAnswers
            ),
            winner
    )
}


fun excludeFromListByUsername(players: List<PlayerDto>, username: String): MutableList<PlayerDto> {
    val result = mutableListOf<PlayerDto>()
    players
            .filter { it.username != null }
            .filter { it.username!!.toLowerCase() != username.toLowerCase() }
            .toCollection(result)

    return result
}

