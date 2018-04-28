package com.stella.game.round.repository

import com.stella.game.round.domain.model.Round
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Repository
interface RoundRepository : CrudRepository<Round, Long>, RoundRepositoryCustom {
    fun findAllByPlayer1UsernameOrPlayer2Username(player1Username: String, player2Username: String): Iterable<Round>
    fun findAllByWinnerName(winnerName: String): Iterable<Round>
}

@Transactional
interface RoundRepositoryCustom {
    fun createRound(
            player1Id: Long,
            player2Id: Long,
            player1Username: String,
            player2Username: String,
            player1CorrectAnswers: Int,
            player2CorrectAnswers: Int,
            winnerName: String) : Long
    fun update(
            player1Username: String,
            player2Username: String,
            player1CorrectAnswers: Int,
            player2CorrectAnswers: Int,
            winnerName: String,
            id: Long) : Boolean

    fun changeWinnerName(id: Long, newWinnerName: String): Boolean

    fun getRoundsByUserName(username: String): Iterable<Round>

    fun getLastRoundByUserName(username: String): Round?
}

open class RoundRepositoryImpl : RoundRepositoryCustom {

    @PersistenceContext
    private lateinit var em: EntityManager

    override fun createRound(player1Id: Long, player2Id: Long, player1Username: String, player2Username: String, player1CorrectAnswers: Int, player2CorrectAnswers: Int, winnerName: String): Long {
        var id = -1L

        val round = Round(
                player1Id,
                player2Id,
                player1Username,
                player2Username,
                player1CorrectAnswers,
                player2CorrectAnswers,
                winnerName,
                ZonedDateTime.now(),
            null
        )
        em.persist(round)

        if (round.id != null) id = round.id!!

        return id
    }

    override fun update(player1Username: String, player2Username: String, player1CorrectAnswers: Int, player2CorrectAnswers: Int, winnerName: String, id: Long): Boolean {
        val round = em.find(Round::class.java, id) ?: return false
        if (winnerName != player1Username && winnerName != player2Username) return false

        if (
                player1Username.isNullOrBlank() ||
                player2Username.isNullOrBlank() ||
                player1CorrectAnswers < 0 ||
                player2CorrectAnswers < 0)
            return false

        round.player1Username = player1Username
        round.player2Username = player2Username
        round.player1CorrectAnswers = player1CorrectAnswers
        round.player2CorrectAnswers = player2CorrectAnswers
        round.winnerName = winnerName

        return true
    }

    override fun changeWinnerName(id: Long, newWinnerName: String): Boolean {
        val round = em.find(Round::class.java, id) ?: return false
        if (newWinnerName.isNullOrBlank()) return false
        round.winnerName = newWinnerName
        return true
    }

    override fun getRoundsByUserName(username: String): Iterable<Round> {
        val query = em.createQuery("select r from Round r where r.player1Username = ?1 OR r.player2Username=?2", Round::class.java)
        query.setParameter(1, username)
        query.setParameter(2, username)
        return query.resultList.toList()
    }

    override fun getLastRoundByUserName(username: String): Round? {
        val query = em.createQuery("select r from Round r where r.player1Username = ?1 OR r.player2Username=?2 ORDER BY r.creationTime DESC", Round::class.java)
        query.setParameter(1, username)
        query.setParameter(2, username)
        return query.setMaxResults(1).singleResult
    }
}