package com.stella.game.round.controller

import com.stella.game.round.domain.converters.RoundConverter
import com.stella.game.round.domain.model.Round
import com.stella.game.round.repository.RoundRepository
import com.stella.game.schema.RoundDto
import io.swagger.annotations.*
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.ConstraintViolationException

@Api(value = "/rounds", description = "API for quiz rounds.")
@RequestMapping(
        path = arrayOf("/rounds"),
        produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE)
)
@RestController
@Validated
class RoundController{
    @Autowired
    private lateinit var crud: RoundRepository

    @RabbitListener(queues = arrayOf("#{queue.name}"))
    fun createRoundRabbit(roundDto: RoundDto){
        registerRound(roundDto)
    }

    @ApiOperation("Delete a round entity with the given id")
    @ApiResponses(
            ApiResponse(code = 400, message = "Given path param is invalid, can not be parsed to long"),
            ApiResponse(code = 404, message = "Round with given id not found"),
            ApiResponse(code = 204, message = "Round with given id was deleted")
    )
    @DeleteMapping(path = arrayOf("/{id}"))
    fun delete(
            @ApiParam(ID_PARAM)
            @PathVariable("id")
            pathId: Long
    ): ResponseEntity<Any> {

        if (!crud.exists(pathId)) {
            return ResponseEntity.status(404).build()
        }
        crud.delete(pathId)
        return ResponseEntity.status(204).build()
    }

    @ApiOperation("Get a single match result specified by id")
    @ApiResponses(
            ApiResponse(code = 400, message = "Given path param is invalid, can not be parsed to long"),
            ApiResponse(code = 404, message = "Round with given id not found"),
            ApiResponse(code = 200, message = "Return round with given id")
    )
    @GetMapping(path = arrayOf("/{id}"))
    fun getRound(
            @ApiParam(ID_PARAM)
            @PathVariable("id") pathId: Long
    ) : ResponseEntity<RoundDto> {
        val dto = crud.findOne(pathId) ?: return ResponseEntity.status(404).build()
        return ResponseEntity.ok(RoundConverter.transform(dto))
    }

    @ApiOperation("Fetch all rounds by default or with specific username if provided in request parameters")
    @ApiResponse(code = 200, message = "List of rounds")
    @GetMapping
    fun getRounds(
            @ApiParam("The specific username as parameter")
            @RequestParam("username", required = false)
            username: String?
    ): ResponseEntity<List<RoundDto>> {

        when(username.isNullOrBlank()){
            true ->
                return ResponseEntity.ok(RoundConverter.transform(crud.findAll()) as List<RoundDto>)
            false ->
                return ResponseEntity.ok(RoundConverter.transform(crud.getRoundsByUserName(username!!)) as List<RoundDto>)
        }
    }

    @ApiOperation("Create a match result")
    @PostMapping(consumes = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    @ApiResponses(
            ApiResponse(code = 201, message = "Round created, return id of new resource"),
            ApiResponse(code = 409, message = "Dto properties does not follow constraints"),
            ApiResponse(code = 400, message = "Dto does not have required properties")
    )
    fun createRound(
            @ApiParam("Round model")
            @RequestBody resultDto: RoundDto
    ) : ResponseEntity<Long> {

        if (!validDto(resultDto)){
            return ResponseEntity.status(400).build()
        }

        try {
            val id = registerRound(resultDto)
            return ResponseEntity.status(201).body(id)
        }
        catch (e: ConstraintViolationException){
            return ResponseEntity.status(409).build()
        }
        catch (e: Exception){
            return ResponseEntity.status(400).build()
        }

    }
    @ApiOperation("Modify the winner name of given round id")
    @ApiResponses(
            ApiResponse(code = 404, message = "Round not found"),
            ApiResponse(code = 400, message = "Update failed while processing"),
            ApiResponse(code = 204, message = "Winner name updated")
    )
    @PatchMapping(path = arrayOf("/{id}"), consumes = arrayOf(MediaType.TEXT_PLAIN_VALUE))
    fun updateWinnerName(
            @ApiParam("The unique id of the round")
            @PathVariable("id")
            id: Long,
            @ApiParam("Winner name")
            @RequestBody
            winnerName: String
    ) : ResponseEntity<Void> {

        // not exist
        if (!crud.exists(id)) {
            return ResponseEntity.status(404).build()
        }

        // not valid winnerName
        if(!crud.changeWinnerName(id,winnerName)){
            return ResponseEntity.status(400).build()
        } else {
            return ResponseEntity.status(204).build()
        }
    }

    @PutMapping(path = arrayOf("/{id}"), consumes = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    fun update(
            @ApiParam(ID_PARAM)
            @PathVariable("id")
            pathId: Long,
            @ApiParam("The round that will replace the old one. Cannot change its id though.")
            @RequestBody
            dto: RoundDto
    ): ResponseEntity<Any> {
        val dtoId: Long

        try {
            dtoId = dto.id!!.toLong()
        } catch (e: Exception) {
            return ResponseEntity.status(400).build()
        }

        if (dtoId != pathId) {
            return ResponseEntity.status(409).build()
        }

        if (!crud.exists(dtoId)) {
            return ResponseEntity.status(404).build()
        }

        if(!updateRound(dto))
            return ResponseEntity.status(400).build()

        return ResponseEntity.status(204).build()
    }


    fun registerRound(resultDto: RoundDto): Long{
        return crud.createRound(
                resultDto.player1!!.id!!.toLong(),
                resultDto.player2!!.id!!.toLong(),
                resultDto.player1!!.username!!,
                resultDto.player2!!.username!!,
                resultDto.player1!!.correctAnswers!!,
                resultDto.player2!!.correctAnswers!!,
                resultDto.winnerName!!,
                resultDto.quiz!!.id!!.toLong(),
                resultDto.quiz!!.question!!,
                resultDto.quiz!!.answers!!,
                resultDto.quiz!!.correctAnswer!!
                )

    }
    fun updateRound(resultDto: RoundDto):Boolean{
        return crud.update(
                resultDto.player1!!.username!!,
                resultDto.player2!!.username!!,
                resultDto.player1!!.correctAnswers!!,
                resultDto.player2!!.correctAnswers!!,
                resultDto.winnerName!!,
                resultDto.id!!.toLong(),
                resultDto.quiz!!.id!!.toLong(),
                resultDto.quiz!!.question!!,
                resultDto.quiz!!.answers!!,
                resultDto.quiz!!.correctAnswer!!
        )
    }

    fun validDto(resultDto: RoundDto): Boolean{
        try {
            resultDto.player1!!.id!!.toLong()
            resultDto.player2!!.id!!.toLong()
        }
        catch (e: Exception){
            return false
        }

        if (
                resultDto.player1?.username!=null &&
                resultDto.player2?.username!=null &&
                resultDto.player1?.correctAnswers!=null &&
                resultDto.player2?.correctAnswers!=null &&
                resultDto.quiz?.id != null &&
                resultDto.quiz?.question != null &&
                resultDto.quiz?.answers != null &&
                resultDto.quiz?.correctAnswer != null &&
                resultDto.winnerName!=null &&
                resultDto.id==null

        )
                { return true }

        return false
    }
}


const val ID_PARAM = "The numeric id of the round"
