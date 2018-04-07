package com.stella.game.quiz.domain.converters

import com.stella.game.quiz.domain.model.Quiz
import com.stella.game.schema.QuizDto

class QuizConverter {

    companion object {

        fun transform(entity: Quiz): QuizDto {

            return QuizDto(
                    question = entity.question,
                    answers = entity.answers,
                    correctAnswer = entity.correctAnswer,
                    subcategoryId = entity.subcategoryId
            ).apply {
                id = entity.id?.toString()
            }
        }

        fun transform(entities: Iterable<Quiz>): List<QuizDto> {
            return entities.map { transform(it) }
        }
    }
}