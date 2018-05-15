package com.stella.game.round

import com.stella.game.round.domain.model.Round
import com.stella.game.round.repository.RoundRepository
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit4.SpringRunner
import javax.validation.ConstraintViolationException

@RunWith(SpringRunner::class)
@DataJpaTest
class RoundRepositoryTest {

    @Autowired
    private lateinit var crud: RoundRepository

    @Before
    fun setup(){
        Assert.assertEquals(0, crud.count())
    }

    private fun createRoundValid() : Long {
        return crud.createRound(
                1,
                2,
                "u1",
                "u2",
                3,
                2,
                "u2")
    }
    private fun createRoundForUsername(username: String) : Long {
        return crud.createRound(
                1,
                2,
                username,
                "u2",
                3,
                2,
                "u2")
    }
    private fun createRound(round: Round): Round? {
        return crud.save(round)
    }

    @Test
    fun testCustomConstraint(){

        try{
            crud.createRound(
                    1,
                    2,
                    "u2",
                    "u2",
                    3,
                    2,
                    "u2")
            Assert.fail()
        }
        catch (e : ConstraintViolationException){}

        // invalid: 1 vs 2 -> winnerName 3
        try{
            crud.createRound(
                    1,
                    2,
                    "u1",
                    "u2",
                    3,
                    2,
                    "u3")
            Assert.fail()
        }
        catch (e : ConstraintViolationException){}
    }


    @Test
    fun testCreateRound_Valid() {

        //Act
        val id = createRoundValid()
        //Assert
        Assert.assertNotNull(id)
        Assert.assertTrue(id != (-1L))
    }

    @Test
    fun testCreate3RoundResults_Valid() {

        //Act
        val id1 = createRoundValid()
        val id2 = createRoundValid()
        val id3 = createRoundValid()

        //Assert
        Assert.assertNotNull(id1)
        Assert.assertNotNull(id2)
        Assert.assertNotNull(id3)
        Assert.assertEquals(3, crud.count())
    }


    @Test
    fun testCreateRound_UsernameNotValid() {

        //Act
        // 1 Blank
        try {
            crud.createRound(
                    1,
                    2,
                    "",
                    "u2",
                    3,
                    2,
                    "u2")
            Assert.fail()
        } catch (e: ConstraintViolationException) {
        }

    }

    @Test
    fun testGetRoundsForUsername(){

        //Arrange
        val username = "Athena"
        createRoundForUsername(username)
        createRoundForUsername(username)
        createRoundForUsername(username)
        createRoundForUsername("Stefan")

        //Act
        // auto-generated
        val list1 : List<Round> = crud.findAllByPlayer1UsernameOrPlayer2Username(username, username) as List<Round>
        // custom
        val list2 : List<Round> = crud.getRoundsByUserName(username) as List<Round>

        //Assert
        Assert.assertEquals(3, list1.size)
        Assert.assertEquals(3, list2.size)
        Assert.assertEquals(4, crud.count())
        Assert.assertTrue(list1.containsAll(list2))
    }


    @Test
    fun testUpdateRound(){
        //Arrange
        val winnerName = "athena"
        val round1 = Round(
                1,
                2,
                winnerName,
                "u2",
                3,
                2,
                winnerName,
                null,
                null
                )
        crud.save(round1)
        val id = round1.id!!

        //Act
        val updated = crud.update(
                "athena",
                "stefan",
                4,
                2,
                "athena",
                id
        )

        //Assert
        Assert.assertTrue(updated)
        Assert.assertTrue(crud.findAll().toList().stream().allMatch { "athena" == it.player1Username })
        Assert.assertTrue(crud.findAll().toList().stream().allMatch { "stefan" == it.player2Username })
        Assert.assertEquals(1, crud.count())
    }

    @Test
    fun testUpdateRound_InvalidInput(){
        //Arrange
        val winnerName = "athena"
        val round1 = Round(
                1,
                2,
                winnerName,
                "u2",
                3,
                2,
                winnerName,
                null,
                null)
        crud.save(round1)
        val id = round1.id!!
        //Act
        val updated =crud.update(
                winnerName,
                " ",
                3,
                2,
                winnerName,
                id)

        //Assert
        Assert.assertFalse(updated)
        Assert.assertTrue(crud.findAll().toList().stream().allMatch { "athena" == it.player1Username })
        Assert.assertTrue(crud.findAll().toList().stream().allMatch { "u2" == it.player2Username })
        Assert.assertEquals(1, crud.count())
    }


    @Test
    fun testUpdateWinnerName(){
        // Arrange
        val oldWinner = "athena"
        val newWinner = "stefan"
        val round1 = Round(
                1,
                2,
                oldWinner,
                "stella",
                3,
                2,
                oldWinner,
                null,
                null)
        crud.save(round1)
        val id = round1.id!!
        Assert.assertEquals(1, crud.count())

        // Act
        // valid
        val winnerNameUpdated = crud.changeWinnerName(id,newWinner)
        // invalid
        val winnerNameUpdated1 = crud.changeWinnerName(id,"")
        // invalid
        val winnerNameUpdated2 = crud.changeWinnerName(123123123123,"123231")

        // Assert
        val roundFromDb = crud.findOne(id)
        Assert.assertTrue(winnerNameUpdated)
        Assert.assertFalse(winnerNameUpdated1)
        Assert.assertFalse(winnerNameUpdated2)
        Assert.assertEquals(newWinner, roundFromDb.winnerName)
    }

    @Test
    fun testGetLastRoundByUserName(){

        //Arrange
        val id1 =  crud.createRound(
                1,
                2,
                "u1",
                "u2",
                3,
                2,
                "u2")
        val id2 =  crud.createRound(
                1,
                2,
                "u1",
                "u2",
                3,
                2,
                "u2")
        val id3 =  crud.createRound(
                1,
                2,
                "u1",
                "u2",
                3,
                2,
                "u2")

        // Act
        val lastRoundResult = crud.getLastRoundByUserName("u1")
        val roundResult1 = crud.findOne(id1)
        val roundResult2 = crud.findOne(id2)

        // Assert
        Assert.assertTrue(roundResult1.creationTime!! < lastRoundResult!!.creationTime)
        Assert.assertTrue(roundResult2.creationTime!! < lastRoundResult!!.creationTime)

        Assert.assertEquals("u1", lastRoundResult.player1Username)
        Assert.assertEquals(2, lastRoundResult.player2CorrectAnswers)
        Assert.assertEquals("u2", lastRoundResult.winnerName)
    }

}