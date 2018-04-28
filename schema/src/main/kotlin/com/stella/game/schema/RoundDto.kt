package com.stella.game.schema

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.io.Serializable
import java.time.ZonedDateTime

@ApiModel("Round representation. Data transfer object represents rounds")
data class RoundDto(

        @ApiModelProperty("Player1 info")
        var player1: PlayerResultDto? = null,

        @ApiModelProperty("Player2 info")
        var player2: PlayerResultDto? = null,

        @ApiModelProperty("Winner name of the round")
        var winnerName: String?= null,

        @ApiModelProperty("Round id")
        var id: String?=null,

        @ApiModelProperty("When the round was created")
        var creationTime: ZonedDateTime? = null
): Serializable


data class PlayerResultDto(
        @ApiModelProperty("Player id")
        var id: String?=null,
        @ApiModelProperty("Player username")
        var username: String?=null,
        @ApiModelProperty("Correct answers")
        var correctAnswers: Int?=null
): Serializable



