package com.stella.game.schema

import io.swagger.annotations.ApiModelProperty
import java.io.Serializable
data class QuizDto(

        @ApiModelProperty("The id of the quiz")
        var id: String? = null,

        @ApiModelProperty("The question of the quiz")
        var question: String? = null,

        @ApiModelProperty("The answers to the quiz")
        var answers: MutableList<String>? = null,

        @ApiModelProperty("The correct answer to the quiz")
        var correctAnswer: Int? = null,

        @ApiModelProperty("Quiz subcategory")
        var subcategoryId: Long? = null
) : Serializable

