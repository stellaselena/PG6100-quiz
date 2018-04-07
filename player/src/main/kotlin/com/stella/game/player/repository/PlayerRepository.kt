package com.stella.game.player.repository

import com.stella.game.player.domain.model.Player
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.lang.Exception
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Repository
interface PlayerRepository : CrudRepository<Player, Long>, PlayerRepositoryCustom {

    fun findAllByUsername(username: String): Iterable<Player>

    fun existsByUsername(username: String): Boolean
}

@Transactional
interface PlayerRepositoryCustom {
    fun createPlayer(
            username: String,
            quizzes: MutableSet<Long>
    ): Long

    fun updatePlayer(
            username: String,
            quizzes: MutableSet<Long>,
            id: Long): Boolean

    fun updateUsername(username: String, id: Long) : Boolean

    fun addQuiz(id: Long, quizId: Long): Boolean
}

open class PlayerRepositoryImpl : PlayerRepositoryCustom {


    @PersistenceContext
    private lateinit var em: EntityManager

    override fun createPlayer(username: String, quizzes: MutableSet<Long>): Long {
        var id: Long = -1
        val playerEntity = Player(
                username,
                quizzes
        )


        em.persist(playerEntity)

        if (playerEntity.id != null) {
            id = playerEntity.id!!
        }

        return id
    }

    override fun updatePlayer(username: String, quizzes: MutableSet<Long>, id: Long): Boolean {
        val player = em.find(Player::class.java, id) ?: return false

        if (username.isNullOrEmpty() || username.length > 50) {
            return false
        }

        player.username = username
        player.quizzes = quizzes

        return true
    }

    override fun updateUsername(username: String, id: Long): Boolean {
        val player = em.find(Player::class.java, id) ?: return false

        if (username.isNullOrEmpty() || username.length > 50) {
            return false
        }

        player.username = username

        return true
    }

    override fun addQuiz(id: Long, quizId: Long): Boolean {
        var added = false
        try {
            val player = em.find(Player::class.java, id)
            added = player.quizzes.add(quizId)
        } catch (e: Exception) {

        }
        return added
    }
}