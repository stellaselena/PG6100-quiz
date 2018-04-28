package com.stella.game.schema.gamelogic

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty


@ApiModel("Dto for players used for searching other players")
data class QuizRoundDto(
        var id: String? = null,

        @ApiModelProperty("The question of the quiz")
        var question: String? = null,

        @ApiModelProperty("The answers to the quiz")
        var answers: MutableList<String>? = null,

        @ApiModelProperty("The correct answer to the quiz")
        var correctAnswer: Int? = null

)