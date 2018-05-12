package com.stella.game.schema

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import java.io.Serializable

@ApiModel("DTO representing Player")
data class PlayerDto(

        @ApiModelProperty("Username of the player")
        var username: String? = null,

        @ApiModelProperty("The id of the player")
        var id: String? = null,

        @ApiModelProperty("Correct answers")
        var correctAnswers : Int? = null


): Serializable