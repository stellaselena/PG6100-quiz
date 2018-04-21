package com.stella.game.gateway.repository

import com.stella.game.gateway.domain.model.UserEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CrudRepository<UserEntity, String> {
    fun findUserByUsername(username: String): UserEntity?
}