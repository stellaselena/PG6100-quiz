package com.stella.game.gamelogic.domain

import com.stella.game.gamelogic.domain.converters.PlayerSearchConverter
import com.stella.game.schema.PlayerDto
import org.junit.Assert
import org.junit.Test

class PlayerSearchConverterTest{
    val playerDto = PlayerDto(
            "username1",
            "1",
            1)

    @Test
    fun testTransform(){

        // Act
        val playerSearchResult = PlayerSearchConverter.transform(playerDto)

        // Assert
        Assert.assertEquals(playerDto.id, playerSearchResult.id)
        Assert.assertEquals(playerDto.username, playerSearchResult.username)
    }
}