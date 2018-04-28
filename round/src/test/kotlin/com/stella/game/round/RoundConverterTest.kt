package com.stella.game.round

import com.stella.game.round.domain.converters.RoundConverter
import com.stella.game.round.domain.model.Round
import com.stella.game.schema.RoundDto
import org.junit.Assert
import org.junit.Test

class RoundConverterTest {
    @Test
    fun testTransform(){

        //Arrange
        val round1 = Round(
                1,
                2,
                "u1",
                "u2",
                3,
                2,
                "u2",
                null,
                null)
        val round2 = Round(
                1,
                2,
                "u1",
                "u2",
                3,
                2,
                "u2",
                null,
                null)


        //Act
        val dto = RoundConverter.transform(round1)
        val list : List<RoundDto> =RoundConverter.transform(listOf(round1,round2)).toList()

        //Assert
        Assert.assertEquals(round1.player2CorrectAnswers, dto.player2!!.correctAnswers)
        Assert.assertNull(dto.id)

    }
}