package com.stella.game.gamelogic.domain.converters

import com.stella.game.gamelogic.domain.model.Question
import com.stella.game.schema.QuizDto

class QuizForRoundConverter{
    companion object {

        fun transform(entity: QuizDto): Question {

            return Question(
                    quizId = entity.id!!.toString(),
                    quizQuestion = entity.question!!.toString(),
                    quizAnswers = entity.answers!!,
                    quizCorrectAnswer = entity.correctAnswer!!

            )
        }

        fun transform(entities: Iterable<QuizDto>): List<Question> {
            return entities.map { transform(it) }
        }
    }
}