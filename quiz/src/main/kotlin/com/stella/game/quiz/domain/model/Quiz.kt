package com.stella.game.quiz.domain.model

import org.hibernate.validator.constraints.NotBlank
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
data class Quiz(

        @get:NotBlank @get:Size(max = 1024)
        var question: String,

        @get:Size(min = 0, max = 4)
        @get:ElementCollection
        var answers: MutableList<String> = mutableListOf(),

        @get:NotNull
        var correctAnswer: Int,

        @get:NotNull
        var subcategoryId: Long,

        @get:Id @get:GeneratedValue
        var id: Long? = null
)