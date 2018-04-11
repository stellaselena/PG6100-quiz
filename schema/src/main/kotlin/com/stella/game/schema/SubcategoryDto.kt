package com.stella.game.schema

import io.swagger.annotations.ApiModelProperty
import java.io.Serializable

data class SubcategoryDto(


        @ApiModelProperty("The id of the subcategory")
        var id: String? = null,

        @ApiModelProperty("The name of the subcategory")
        var name: String? = null,

        @ApiModelProperty("The category of the subcategory")
        var category: Long? = null

) : Serializable