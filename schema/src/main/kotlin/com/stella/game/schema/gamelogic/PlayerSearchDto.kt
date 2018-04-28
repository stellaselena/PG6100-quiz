package com.stella.game.schema.gamelogic

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel("Dto for players used for searching other players")
data class PlayerSearchDto(
        @ApiModelProperty("Player id")
        var id : String? = null,
        @ApiModelProperty("Player username")
        var username : String? = null

)