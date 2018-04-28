package com.stella.game.gamelogic.services

import com.stella.game.gamelogic.domain.model.Log
import com.stella.game.gamelogic.domain.model.Participant
import com.stella.game.gamelogic.domain.model.Question
import com.stella.game.schema.gamelogic.QuizResultLogDto
import org.springframework.stereotype.Service

@Service
class QuizMockService{

    fun startRound(player1: Participant, player2: Participant, questions: List<Question> ): QuizResultLogDto {
        val gameLog: MutableMap<Int, String> = mutableMapOf()
        var counter = 1
        while(!isRoundEnded(player1, player2)){ //any player has >= 2 correct answers and has more correct answers than the other opponent
                val question = questions[randomQuestion(questions.size-1)]
                val player1Answer = mockAnswer()
                val player2Answer = mockAnswer()

                if(player1Answer == question.quizCorrectAnswer){
                    player1.correctAnswers = player1.correctAnswers + 1
                    val logLine = Log(player1,player2, question)
                    gameLog.put(counter++,logLine.toString())
                }
                if(player2Answer == question.quizCorrectAnswer){
                    player2.correctAnswers = player2.correctAnswers + 1
                    val logLine = Log(player2,player1, question)
                    gameLog.put(counter++,logLine.toString())
                }

                if(player1.correctAnswers == player2.correctAnswers){
                    // do nothing
                }
            }
        val winner =  if(player1.correctAnswers > player2.correctAnswers) player1.username else player2.username
        return QuizResultLogDto(player1.username, player2.username, winner, gameLog)
    }


    fun isRoundEnded(player1: Participant, player2: Participant) : Boolean{
        return(
                player1.correctAnswers >= 2 && player1.correctAnswers > player2.correctAnswers ||
                player2.correctAnswers >= 2 && player2.correctAnswers > player1.correctAnswers)
    }

    private fun mockAnswer():Int{
        return (Math.random()*4).toInt()+1
    }
    private fun randomQuestion(size : Int):Int{
        return (Math.random()*size).toInt()+1
    }


}