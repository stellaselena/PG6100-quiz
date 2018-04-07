package com.stella.game.category.domain.model

import org.hibernate.validator.constraints.NotBlank
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.Size

@Entity
data class Category(

        @get:NotBlank
        @get:Size(max = 32)
        var name: String,

        @get:Id
        @get:GeneratedValue
        var id: Long? = null

)