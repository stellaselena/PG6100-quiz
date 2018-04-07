package com.stella.game.player.domain.converter

import com.stella.game.player.domain.model.Player
import com.stella.game.schema.PlayerDto

class PlayerConverter {

    companion object {

        fun transform(entity: Player): PlayerDto {
            return PlayerDto(
                    username = entity.username,
                    id = entity.id.toString(),
                    quizzes = entity.quizzes
            )
        }

        fun transform(entities: Iterable<Player>): Iterable<PlayerDto> {
            return entities.map { transform(it) }
        }
    }
}