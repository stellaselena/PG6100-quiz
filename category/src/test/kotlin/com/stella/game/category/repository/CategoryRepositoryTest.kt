package com.stella.game.category.repository

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest

import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataJpaTest
class EntityRepositoryImplTest {

    @Autowired
    private lateinit var repo : CategoryRepository


    @Test
    fun testCreateCategoryValid()
    {
        val id = repo.createCategory("name")
        Assert.assertNotNull(id)
        Assert.assertTrue(id != (-1L))
    }


}