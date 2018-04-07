package com.stella.game.player.domain.model

import org.hibernate.validator.constraints.NotBlank
import javax.persistence.*

import javax.validation.constraints.Size

@Entity
data class Player(

        @get:NotBlank
        @get:Size(max = 50)
        @get:Column(unique = true)
        var username: String,

        @get:ElementCollection
        var quizzes: MutableSet<Long> = mutableSetOf(),

        @get:Id
        @get: GeneratedValue
        var id: Long? = null
)
