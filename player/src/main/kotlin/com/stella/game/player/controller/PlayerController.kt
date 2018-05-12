package com.stella.game.player.controller

import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.stella.game.player.repository.PlayerRepository
import com.stella.game.player.domain.converter.PlayerConverter
import com.stella.game.schema.PlayerDto
import com.stella.game.schema.QuizDto
import io.swagger.annotations.*
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import java.net.URI
import javax.validation.ConstraintViolationException

@Api(value = "/players", description = "API for player entities")
@RequestMapping(
        path = arrayOf("/players"),
        produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE)
)
@RestController
@Validated
class PlayerController {

    @Autowired
    private lateinit var repo: PlayerRepository

    @Autowired
    private lateinit var rest: RestTemplate

    @Value("\${quizServerName}")
    private lateinit var quizHost : String

    @RabbitListener(queues = arrayOf("#{queue.name}"))
    fun createPlayerRabbit(playerDto: PlayerDto) {

        try {
            repo.createPlayer(
                    username = playerDto.username!!.toLowerCase()
            )
        } catch (e: Exception) { }
    }

    @ApiOperation("Create new player")
    @PostMapping(consumes = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    @ApiResponses(
            ApiResponse(code = 201, message = "Id of created player"),
            ApiResponse(code = 400, message = "Something wrong with the player-body"),
            ApiResponse(code = 409, message = "Username is not unique")
    )
    fun createPlayer(
            @ApiParam("Player to save")
            @RequestBody
            playerDto: PlayerDto): ResponseEntity<Long> {

        if (!playerDto.id.isNullOrEmpty()) {
            return ResponseEntity.status(400).build()
        }

        if (!isDtoFieldsNotNull(playerDto)) {
            return ResponseEntity.status(400).build()
        }

        playerDto.username = playerDto.username!!.toLowerCase()

        if (repo.existsByUsername(playerDto.username!!)) {
            return ResponseEntity.status(409).build()
        }

        try {
            val savedId = repo.createPlayer(
                    username = playerDto.username!!
            )
            return ResponseEntity.status(201).body(savedId)

        } catch (e: java.lang.Exception) {
            return ResponseEntity.status(400).build()
        }
    }

    @ApiOperation("Get player specified by id")
    @GetMapping(path = arrayOf("/{id}"))
    @ApiResponses(
            ApiResponse(code = 400, message = "Id isn't a number"),
            ApiResponse(code = 404, message = "Could not find player")
    )
    fun getPlayerById(@ApiParam("Id of Player")
                      @PathVariable("id")
                      pathId: String?)
            : ResponseEntity<PlayerDto> {
        val id: Long
        try {
            id = pathId!!.toLong()
        } catch (e: Exception) {
            return ResponseEntity.status(400).build()
        }

        val dto = repo.findOne(id) ?: return ResponseEntity.status(404).build()
        return ResponseEntity.ok(PlayerConverter.transform(dto))
    }

    @ApiOperation("Fetch all players. Can be filtered by username")
    @GetMapping
    fun getAllPlayers(
            @ApiParam("Username of player")
            @RequestParam(name = "username", required = false)
            username: String?
    ): ResponseEntity<Iterable<PlayerDto>> {

        if (username != null) {
            val lowercaseUsername = username.toLowerCase()
            return ResponseEntity.ok(PlayerConverter.transform(repo.findAllByUsername(lowercaseUsername)))
        }

        return ResponseEntity.ok(PlayerConverter.transform(repo.findAll()))

    }

    @ApiOperation("Replace the data of a player")
    @PutMapping(path = arrayOf("/{id}"), consumes = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    @ApiResponses(
            ApiResponse(code = 400, message = "Something wrong player-body sent in this request"),
            ApiResponse(code = 404, message = "Could not find player by this id"),
            ApiResponse(code = 409, message = "Cannot change the id of player in the body!")
    )
    fun updatePlayer(
            @ApiParam("Id defining the player.")
            @PathVariable("id")
            pathId: String?,

            @ApiParam("Data to replace old player. Id cannot be changed, and must be the same in path and RequestBody")
            @RequestBody
            playerDto: PlayerDto
    ): ResponseEntity<Long> {

        val id: Long
        try {
            id = pathId!!.toLong()
        } catch (e: Exception) {
            return ResponseEntity.status(404).build()
        }

        // Don't change ID
        if (playerDto.id != pathId) {
            return ResponseEntity.status(409).build()
        }
        if (!repo.exists(id)) {
            return ResponseEntity.status(404).build()
        }
        if (!isDtoFieldsNotNull(playerDto)) {
            return ResponseEntity.status(400).build()
        }

        try {
            val successful = repo.updatePlayer(
                    playerDto.username!!,
                    playerDto.correctAnswers!!,
                    playerDto.id!!.toLong()
            )
            if (!successful) {
                return ResponseEntity.status(400).build()
            }
            return ResponseEntity.status(204).build()
        } catch (e: ConstraintViolationException) {
            return ResponseEntity.status(400).build()
        }
    }

    @ApiOperation("Replace the username of a player")
    @PatchMapping(path = arrayOf("/{id}"), consumes = arrayOf(MediaType.TEXT_PLAIN_VALUE))
    @ApiResponses(
            ApiResponse(code = 204, message = "Currency successfully update. No content to return"),
            ApiResponse(code = 400, message = "Something wrong with new currency value"),
            ApiResponse(code = 404, message = "Could not find player to update currency for.")
    )
    fun updateUsername(
            @ApiParam("Id defining the player.")
            @PathVariable("id")
            id: Long,

            @ApiParam("New currency for player. Currency cannot be negative.")
            @RequestBody
            username: String
    ): ResponseEntity<Void> {

        if (!repo.exists(id)) {
            return ResponseEntity.status(404).build()
        }

        if (username.isNullOrBlank()) {
            return ResponseEntity.status(400).build()
        }
        if (!repo.updateUsername(username, id)) {
            return ResponseEntity.status(400).build()
        }
        return ResponseEntity.status(204).build()
    }


    @ApiOperation("Delete player by id")
    @DeleteMapping(path = arrayOf("/{id}"))
    @ApiResponses(
            ApiResponse(code = 204, message = "No content, player successfully deleted"),
            ApiResponse(code = 400, message = "Id is not a number"),
            ApiResponse(code = 404, message = "Could not find player")
    )
    fun deletePlayerById(
            @ApiParam("Id of player to delete")
            @PathVariable("id")
            pathId: Long?
    ): ResponseEntity<Any> {

        val id: Long
        try {
            id = pathId!!.toLong()
            repo.delete(id)
        } catch (e: NumberFormatException) {
            return ResponseEntity.status(400).build()
        } catch (e1: java.lang.Exception) {
            return ResponseEntity.status(404).build()
        }

        return ResponseEntity.status(204).build()
    }

    private fun isDtoFieldsNotNull(playerDto: PlayerDto): Boolean {
        if (playerDto.username.isNullOrBlank()) {
            return false
        }

        return true

    }
}