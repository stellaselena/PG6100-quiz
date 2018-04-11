package com.stella.game.schema

import io.swagger.annotations.ApiModelProperty

data class CategoryDto(

        @ApiModelProperty("The id of the category")
        var id: String? = null,

        @ApiModelProperty("The name of the category")
        var name: String? = null

)