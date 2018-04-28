package com.stella.game.schema.gamelogic

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("Dto for quiz log")
data class QuizResultLogDto(
        @ApiModelProperty("Player 1 username")
        var player1Username: String? = null,
        @ApiModelProperty("Player 2 username")
        var player2Username: String? = null,
        @ApiModelProperty("Quiz winner")
        var winner: String? = null,
        @ApiModelProperty("Quiz log")
        var quizLog: MutableMap<Int,String>? = null
)