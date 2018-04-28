package com.stella.game.gamelogic.domain.converters

import com.stella.game.gamelogic.domain.model.Participant
import com.stella.game.schema.PlayerDto

class PlayerQuizRoundConverter{
    companion object {
        fun transform(player : PlayerDto) : Participant{
            return Participant(
                    playerId = player.id.toString(),
                    username = player.username.toString(),
                    correctAnswers = player.correctAnswers!!
            )
        }
    }
}