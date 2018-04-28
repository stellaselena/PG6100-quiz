package com.stella.game.round.domain.converters
import com.stella.game.round.domain.model.Round
import com.stella.game.schema.PlayerResultDto
import com.stella.game.schema.RoundDto

class RoundConverter {
    companion object {
        fun transform(entity: Round) : RoundDto {
            return RoundDto(
                    id = entity.id?.toString(),
                    player1 = PlayerResultDto(
                            entity.player1Id.toString(),
                            entity.player1Username,
                            entity.player1CorrectAnswers),
                    player2 = PlayerResultDto(
                            entity.player2Id.toString(),
                            entity.player2Username,
                            entity.player2CorrectAnswers),
                    winnerName = entity.winnerName,
                    creationTime = entity.creationTime
            )
        }
        fun transform(entities: Iterable<Round>) : Iterable<RoundDto>{
            return entities.map { transform(it) }
        }
    }
}