package com.stella.game.subcategory.controller

import com.stella.game.schema.CategoryDto
import com.stella.game.schema.SubcategoryDto
import com.stella.game.subcategory.domain.converter.SubcategoryConverter
import com.stella.game.subcategory.repository.SubcategoryRepository
import io.swagger.annotations.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.netflix.ribbon.RibbonClient
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.lang.Deprecated
import javax.validation.ConstraintViolationException

@Api(value = "/subcategories", description = "API for subcategory data.")
@RestController
@RequestMapping(
        path = arrayOf("/subcategories"),
        produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE)
)
@Validated
class SubcategoryApi {

    @Autowired
    private lateinit var rest: RestTemplate

    @Autowired
    private lateinit var repo: SubcategoryRepository

    @Value("\${categoryServerName}")
    private lateinit var categoryHost : String

    @ApiOperation("Get all the subcategories")
    @GetMapping
    fun get(): ResponseEntity<List<SubcategoryDto>> {

        return ResponseEntity.ok(SubcategoryConverter.transform(repo.findAll()))
    }

    @ApiOperation("Create a subcategory")
    @PostMapping()
    @ApiResponse(code = 201, message = "The id of newly created subcategory")
    fun createSubcategory(
            @ApiParam("Subcategory name and category. Should not specify id")
            @RequestBody
            dto: SubcategoryDto)
            : ResponseEntity<Long> {

        if (!(dto.id.isNullOrEmpty())) {
            return ResponseEntity.status(400).build()
        }

        if (dto.name == null || dto.category == null ) {
            return ResponseEntity.status(400).build()
        }

        // check if category id exists
        val categoryURL = "${categoryHost}/categories/${dto.category}"
        val response: ResponseEntity<CategoryDto> = try {
            rest.getForEntity(categoryURL, CategoryDto::class.java)
        } catch (e: HttpClientErrorException) {
            return ResponseEntity.status(404).build()
        }

        if (response.statusCodeValue != 200) {
            return ResponseEntity.status(400).build()
        }

        val id: Long?
        try {
            id = repo.createSubcategory(dto.name!!, dto.category!!)
        } catch (e: ConstraintViolationException) {
            return ResponseEntity.status(400).build()
        }

        if (id == null) {
            return ResponseEntity.status(500).build()
        }

        return ResponseEntity.status(201).body(id)
    }


    @ApiOperation("Update a existing subcategory")
    @PutMapping(path = arrayOf("/{id}"), consumes = arrayOf(MediaType.APPLICATION_JSON_VALUE))
    fun update(
            @ApiParam("Subcategory id")
            @PathVariable("id")
            id: Long?,
            //
            @ApiParam("The subcategory that will replace the old one. Cannot change its id though.")
            @RequestBody
            dto: SubcategoryDto
    ): ResponseEntity<Any> {
        if (id == null) {
            return ResponseEntity.status(400).build()
        }
        if (!repo.exists(id)) {
            return ResponseEntity.status(404).build()
        }

        if (dto.name == null) {
            return ResponseEntity.status(400).build()
        }
        try {
            repo.update(id, dto.name!!, dto.category!!)
        } catch (e: ConstraintViolationException) {
            return ResponseEntity.status(400).build()
        }

        return ResponseEntity.status(204).build()



    }


    @ApiOperation("Delete a subcategory with the given id")
    @DeleteMapping(path = arrayOf("/{id}"))
    fun delete(@ApiParam("Subcategory id")
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

    @ApiOperation("Get a single subcategory specified by id")
    @GetMapping(path = arrayOf("/{id}"))
    fun getSubcategory(@ApiParam("Subcategory id")
                       @PathVariable("id")
                       pathId: String?)
            : ResponseEntity<SubcategoryDto> {

        val id: Long
        try {
            id = pathId!!.toLong()
        } catch (e: Exception) {

            return ResponseEntity.status(404).build()
        }

        val dto = repo.findOne(id) ?: return ResponseEntity.status(404).build()

        return ResponseEntity.ok(SubcategoryConverter.transform(dto))
    }

}