package com.stella.game.category.controller

import com.stella.game.category.domain.converter.CategoryConverter
import com.stella.game.category.repository.CategoryRepository
import com.stella.game.schema.CategoryDto
import io.swagger.annotations.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import org.springframework.web.util.UriComponentsBuilder
import java.lang.Deprecated
import javax.validation.ConstraintViolationException

@Api(value = "/categories", description = "API for category data.")
@RestController
@RequestMapping(
        path = arrayOf("/categories"),
        produces = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE)
)
@Validated

class CategoryApi {

    @Autowired
    private lateinit var repo: CategoryRepository


    @ApiOperation("Retrieve all categories")
    @GetMapping
    fun get()
            : ResponseEntity<List<CategoryDto>> {

        val list = repo.findAll()

        return ResponseEntity.ok(CategoryConverter.transform(list))


    }

    @ApiOperation("Create a category")
    @PostMapping(consumes = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    @ApiResponse(code = 201, message = "The id of newly created category")
    fun createCategory(
            @ApiParam("Category name. Should not specify id")
            @RequestBody
            dto: CategoryDto)
            : ResponseEntity<Long> {

        if (!(dto.id.isNullOrEmpty())) {
            //Cannot specify id for a newly generated news
            return ResponseEntity.status(400).build()
        }

        if (dto.name == null ) {
            return ResponseEntity.status(400).build()
        }

        val id: Long?
        try {
            id = repo.createCategory(dto.name!!)
        } catch (e: ConstraintViolationException) {
            return ResponseEntity.status(400).build()
        }

        if (id == null) {
            return ResponseEntity.status(500).build()
        }

        return ResponseEntity.status(201).body(id)
    }

    @ApiOperation("Update an existing category")
    @PutMapping(path = arrayOf("/{id}"), consumes = arrayOf(MediaType.APPLICATION_JSON_UTF8_VALUE))
    fun update(
            @ApiParam("Category Id")
            @PathVariable("id")
            id: Long?,
            //
            @RequestBody
            dto: CategoryDto
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
            repo.update(id, dto.name!!)
        } catch (e: ConstraintViolationException) {
            return ResponseEntity.status(400).build()
        }

        return ResponseEntity.status(204).build()


    }


    @ApiOperation("Delete a category with the given id")
    @DeleteMapping(path = arrayOf("/{id}"))
    fun delete(@ApiParam("Category id")
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


    @ApiOperation("Get a single category specified by id")
    @GetMapping(path = arrayOf("/{id}"))
    fun getCategory(@ApiParam("Category id")
                    @PathVariable("id")
                    pathId: String?)
            : ResponseEntity<CategoryDto> {

        val id: Long
        try {
            id = pathId!!.toLong()
        } catch (e: Exception) {

            return ResponseEntity.status(404).build()
        }

        val dto = repo.findOne(id) ?: return ResponseEntity.status(404).build()

        return ResponseEntity.ok(CategoryConverter.transform(dto))
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

}