package com.stella.game.gamelogic.services

import com.stella.game.gamelogic.domain.model.Participant
import com.stella.game.gamelogic.domain.model.Question
import com.stella.game.schema.gamelogic.QuizResultLogDto
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class QuizMockServiceTest {
    private var player1: Participant? = null
    private var player2: Participant? = null
    private var questions: List<Question> = listOf()

    @Before
    fun init() {
        player1 = Participant("1", "A", 0)
        player2 = Participant("2", "B", 0)
        questions = listOf(Question("1", "q1", mutableListOf("a", "b", "c", "d"), 1),
                Question("2", "q2", mutableListOf("a", "b", "c", "d"), 1),
                Question("3", "q3", mutableListOf("a", "b", "c", "d"), 1),
                Question("4", "q4", mutableListOf("a", "b", "c", "d"), 1),
                Question("5", "q5", mutableListOf("a", "b", "c", "d"), 1),
                Question("6", "q6", mutableListOf("a", "b", "c", "d"), 1))


    }

    @After
    fun tearDown() {
        player1 = null
        player2 = null
    }

    @Test
    fun roundTest() {
        var gameService = QuizMockService()

        val roundResult: QuizResultLogDto = gameService.startRound(player1 = player1!!, player2 = player2!!, questions = questions!!)
        Assert.assertTrue(roundResult.quizLog!!.size >= 2)
        Assert.assertTrue(roundResult.winner!! == "A" || roundResult.winner!! == "B")

    }
}