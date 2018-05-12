package com.stella.game.player.domain.model

import org.hibernate.validator.constraints.NotBlank
import javax.persistence.*
import javax.validation.constraints.Min
import javax.validation.constraints.NotNull

import javax.validation.constraints.Size

@Entity
data class Player(

        @get:NotBlank
        @get:Size(max = 50)
        @get:Column(unique = true)
        var username: String,

        @get:Min(0)
        var correctAnswers: Int? = null,

        @get:Id
        @get: GeneratedValue
        var id: Long? = null
)
