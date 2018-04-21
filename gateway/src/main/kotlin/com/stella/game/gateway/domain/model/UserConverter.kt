package com.stella.game.gateway.domain.model

class UserConverter {
    companion object {

        fun transform(entity: UserEntity): UserDto {
            return UserDto(
                    username = entity.username,
                    password = entity.password,
                    roles = entity.roles,
                    enabled = entity.enabled
            )
        }

        fun transform(entities: Iterable<UserEntity>): Iterable<UserDto> {
            return entities.map { transform(it) }
        }
    }
}