package com.stella.game.quiz.domain.model

import org.hibernate.validator.constraints.NotBlank
import org.hibernate.validator.constraints.Range
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

        @get:Range(min = 0, max = 100)
        var subcategoryId: Long,

        @get:Id @get:GeneratedValue
        var id: Long? = null
)