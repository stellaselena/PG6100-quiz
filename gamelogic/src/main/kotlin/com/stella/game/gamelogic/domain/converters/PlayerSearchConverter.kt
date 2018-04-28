package com.stella.game.gamelogic.domain.converters

import com.stella.game.schema.PlayerDto
import com.stella.game.schema.gamelogic.PlayerSearchDto

class PlayerSearchConverter {

    companion object {
        fun transform(entity: PlayerDto): PlayerSearchDto {
            return PlayerSearchDto(
                    id = entity.id.toString(),
                    username = entity.username
            )
        }

        fun transform(entities: Iterable<PlayerDto>): Iterable<PlayerSearchDto> {
            return entities.map { transform(it) }
        }
    }
}