package com.stella.game.round.domain.model

import org.hibernate.validator.constraints.NotBlank
import org.hibernate.validator.constraints.Range
import java.time.ZonedDateTime
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size
import javax.validation.constraints.Min


@Entity
@RoundConstraint
data class Round(
        @get: NotNull
        var player1Id: Long,
        @get: NotNull
        var player2Id: Long,
        @get: NotBlank @get:Size(max=32)
        var player1Username: String,
        @get: NotBlank @get:Size(max=32)
        var player2Username: String,
        @get:Min(0)
        var player1CorrectAnswers : Int,
        @get:Min(0)
        var player2CorrectAnswers : Int,
        var winnerName: String? = null,
        var creationTime: ZonedDateTime? = null,
        @get:Id @get:GeneratedValue
        var id: Long? = null,
        @get:NotNull
        var quizId: Long? = null,
        @get: NotBlank @get:Size(max =32)
        var quizQuestion: String? = null,
        @get:Size(min = 0, max = 4)
        @get:ElementCollection
        var quizAnswers: MutableList<String>,
        @get:NotNull
        var quizCorrectAnswer: Int
)