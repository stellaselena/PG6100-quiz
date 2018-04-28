package com.stella.game.gamelogic.domain.model

class Question(
        var quizId: String,
        var quizQuestion: String,
        var quizAnswers: MutableList<String> = mutableListOf(),
        var quizCorrectAnswer: Int
)