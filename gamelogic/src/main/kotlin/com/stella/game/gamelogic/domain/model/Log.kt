package com.stella.game.gamelogic.domain.model

class Log(
        private var winner: Participant,
        private var loser: Participant,
        private var quiz: Question
){
    override fun toString(): String {
        return "Question:${quiz.quizQuestion}" +
                "| Answers: 1. ${quiz.quizAnswers[0]} 2. ${quiz.quizAnswers[1]} 3. ${quiz.quizAnswers[2]} 4. ${quiz.quizAnswers[3]}" +
                "| ${winner.username} has ${winner.correctAnswers} correct answers => ${loser.username} has ${loser.correctAnswers} correct answers" +
                "| The correct answer was answer nr ${quiz.quizCorrectAnswer}"
    }
}

