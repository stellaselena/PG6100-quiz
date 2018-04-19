package com.stella.game.quiz.controller

import com.stella.game.quiz.domain.converters.QuizConverter
import com.stella.game.quiz.domain.model.Quiz
import com.stella.game.quiz.repository.QuizRepository
import com.stella.game.schema.QuizDto
import io.swagger.annotations.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import javax.validation.ConstraintViolationException
import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixCommandGroupKey
import com.stella.game.schema.SubcategoryDto
import org.springframework.web.client.RestTemplate
import java.util.logging.Logger


@Api(value = "/quizzes", description = "Handling of creating and retrieving quizzes")
@RequestMapping(
        path = arrayOf("/quizzes"),
        produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE)
)
@RestController
@Validated
class QuizController {

    private val logger : Logger = Logger.getLogger(QuizController::class.java.canonicalName)


    @Autowired
    private lateinit var rest: RestTemplate

    @Value("\${subcategoryServerName}")
    private lateinit var subcategoryHost : String
    @Autowired
    private lateinit var repo: QuizRepository




    @ApiOperation("Get all the quizzes")
    @GetMapping
    fun getQuizzes(@ApiParam("Subcategory name")
            @RequestParam("subcategory", required = false)
            subcategoryId: Long?

    ): ResponseEntity<List<QuizDto>> {

        val list = if (subcategoryId != null) {
            repo.findAllBySubcategoryId(subcategoryId)
        } else {
            repo.findAll()
        }

        return ResponseEntity.ok(QuizConverter.transform(list))
    }
    @ApiOperation("Get a single quiz specified by id")
    @GetMapping(path = arrayOf("/{id}"))
    fun getQuiz(@ApiParam("Quiz id")
                @PathVariable("id")
                pathId: String?)
            : ResponseEntity<QuizDto> {

        val id: Long
        try {
            id = pathId!!.toLong()
        } catch (e: Exception) {

            return ResponseEntity.status(404).build()
        }

        val dto = repo.findOne(id) ?: return ResponseEntity.status(404).build()
        return ResponseEntity.ok(QuizConverter.transform(dto))

    }

    private inner class CallGetSubcategory(private val subcategoryId: Long)
        : HystrixCommand<Int>(HystrixCommandGroupKey.Factory.asKey("Call get subcategory")) {

        override fun run(): Int {

            val subcategoryURL = "${subcategoryHost}/subcategories/${subcategoryId}"
            val result = rest.getForEntity(subcategoryURL, SubcategoryDto::class.java)

            return result.statusCodeValue
        }

        override fun getFallback(): Int {
            return 0
        }
    }



    @ApiOperation("Create a quiz")
    @PostMapping(consumes = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    @ApiResponse(code = 201, message = "The id of newly created quiz")
    fun createQuiz(
            @ApiParam("Question of the quiz, answers, correct answer and subcategory. Should not specify id")
            @RequestBody
            dto: QuizDto)
            : ResponseEntity<Long> {

        if (!(dto.id.isNullOrEmpty())) {
            //Cannot specify id for a newly generated news
            return ResponseEntity.status(400).build()
        }

        if (dto.question == null || dto.answers == null || dto.correctAnswer == null || dto.subcategoryId == null) {
            return ResponseEntity.status(400).build()
        }

        val result = CallGetSubcategory(dto.subcategoryId!!).execute()

        if (result != 200 || result == 0) {
            return ResponseEntity.status(400).build()
        }
        val id: Long?
        try {
            id = repo.createQuiz(dto.question!!, dto.answers!!, dto.correctAnswer!!, dto.subcategoryId!!)
        } catch (e: ConstraintViolationException) {
            return ResponseEntity.status(400).build()
        }

        if (id == null) {
            return ResponseEntity.status(500).build()
        }

        return ResponseEntity.status(201).body(id)
    }


    @ApiOperation("Delete a quiz with the given id")
    @DeleteMapping(path = arrayOf("/{id}"))
    fun delete(@ApiParam("Quiz id")
               @PathVariable("id")
               pathId: String?): ResponseEntity<Any> {

        val id: Long
        try {
            id = pathId!!.toLong()
        } catch (e: Exception) {
            return ResponseEntity.status(400).build()
        }

        if (!repo.exists(id)) {
            return ResponseEntity.status(404).build()
        }

        repo.delete(id)
        return ResponseEntity.status(204).build()
    }

    @ExceptionHandler(value = ConstraintViolationException::class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    fun handleValidationFailure(ex: ConstraintViolationException): String {

        val messages = StringBuilder()

        for (violation in ex.constraintViolations) {
            messages.append(violation.message + "\n")
        }

        return messages.toString()
    }

    @ApiOperation("Update an existing question")
    @PutMapping(path = arrayOf("/{id}"), consumes = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    fun update(
            @ApiParam("Quiz id")
            @PathVariable("id")
            id: Long?,
            //
            @RequestBody
            dto: QuizDto
    ): ResponseEntity<Any> {
        if (id == null) {
            return ResponseEntity.status(400).build()
        }

        if (!repo.exists(id)) {
            return ResponseEntity.status(404).build()
        }

        if (dto.question == null || dto.answers == null || dto.correctAnswer == null || dto.subcategoryId == null) {
            return ResponseEntity.status(400).build()
        }

        val result = CallGetSubcategory(dto.subcategoryId!!).execute()

        if (result != 200 || result == 0) {
            return ResponseEntity.status(400).build()
        }

        try {
            repo.update(id, dto.question!!, dto.answers!!, dto.correctAnswer!!, dto.subcategoryId!!)
        } catch (e: ConstraintViolationException) {
            return ResponseEntity.status(400).build()
        }

        return ResponseEntity.status(204).build()


    }

    @ApiOperation("Update the question content of an existing quiz")
    @PatchMapping(path = arrayOf("/{id}/question"), consumes = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    fun updateQuestion(
            @ApiParam("Quiz id")
            @PathVariable("id")
            id: Long?,
            //
            @ApiParam("The new question which will replace the old one")
            @RequestBody
            question: String
    ): ResponseEntity<Any> {
        if (id == null) {
            return ResponseEntity.status(400).build()
        }

        if (!repo.exists(id)) {
            return ResponseEntity.status(404).build()
        }

        try {
            repo.updateQuestion(id, question)
        } catch (e: ConstraintViolationException) {
            return ResponseEntity.status(400).build()
        }

        return ResponseEntity.status(204).build()
    }

    @ApiOperation("Get a random quiz with option for specifying subcategories")
    @GetMapping(path = arrayOf("/randomQuiz"), consumes = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    fun getRandom(
                  @ApiParam("with subcategory option")
                  @RequestParam(value = "subcategory", required = false) subcategoryId: Long?
    ): ResponseEntity<QuizDto> {

        val quizzes = repo.findAll()
        var quizId = repo.findARandomQuiz(quizzes).id

        if (subcategoryId != null) {
            try {

                quizId = repo.findRandomQuizWithSubcategory(quizzes, subcategoryId)!!.id
                val dto = repo.findOne(quizId)
                return ResponseEntity.ok(QuizConverter.transform(dto))



            } catch (e:  IllegalArgumentException) {
                return ResponseEntity.status(404).build()
            }

        } else {
            if (quizId!= null){
                val dto = repo.findOne(quizId)
                return ResponseEntity.ok(QuizConverter.transform(dto))

            } else {
                return ResponseEntity.status(404).build()
            }
        }

    }

    @ApiOperation("Get random quizzes")
    @GetMapping(path = arrayOf("/randomQuizzes"))
    fun getRandomQuizzes(
            @ApiParam("Subcategory name")
            @RequestParam(value = "subcategory", required = false)
            subcategory: Long?,
            @ApiParam("Size") @RequestParam("n", required = false, defaultValue = "2") n: Int?)
            : ResponseEntity<List<QuizDto>> {

        val quizzes = repo.findAll()
        val size = if (n!! > 2) {
            n
        } else {
            2
        }
        val list: MutableList<Quiz> = listOf<Quiz>().toMutableList()

        if (subcategory != null) {
            for (i in 1..size) {
                try {
                    list.add(repo.findRandomQuizWithSubcategory(quizzes, subcategory)!!)

                } catch (e: IllegalArgumentException) {
                    return ResponseEntity.status(404).build()
                }
                return when {
                    list.distinct().count() > size -> ResponseEntity.ok(QuizConverter.transform(list))
                    else -> ResponseEntity.status(404).build()
                }

            }

        } else  {

            try{
                for (i in 1..size) {
                    list.add(repo.findARandomQuiz(quizzes))
                }


            } catch (e:IllegalArgumentException){
                return ResponseEntity.status(404).build()

            }
            return when {
                list.distinct().count() > size -> ResponseEntity.ok(QuizConverter.transform(list))
                else -> ResponseEntity.status(404).build()
            }


        }
        return ResponseEntity.status(404).build()

    }



}