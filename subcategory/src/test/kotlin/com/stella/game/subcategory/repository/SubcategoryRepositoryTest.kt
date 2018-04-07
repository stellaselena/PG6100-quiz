package com.stella.game.subcategory.repository

import com.stella.game.subcategory.domain.model.Subcategory
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@DataJpaTest
class EntityRepositoryImplTest {


    @Autowired
    private lateinit var repo: SubcategoryRepository

    @Before
    fun cleanDatabase(){
        repo.deleteAll()

    }

    @Test
    fun testInitialization() {
        Assert.assertNotNull(repo)
    }

    fun createSubcategory():Subcategory{
        val id =  repo.createSubcategory("tennis", 1)
        return repo.findOne(id)
    }

    fun createSomeSubcategories(): Iterable<Subcategory>{
        val id1 =  repo.createSubcategory("tennis", 1L)
        val id2 =  repo.createSubcategory("tennis", 1L)
        val id3 =  repo.createSubcategory("tennis", 2L)

        val subIds = listOf(id1, id2, id3)
        val subcategories = listOf<Subcategory>().toMutableList()
        for(i in subIds){
            subcategories.add(repo.findOne(i))
        }
        return subcategories
    }

    @Test
    fun testCreateSubcategory(){


        val quiz = createSubcategory()
        Assert.assertEquals(1, repo.count())
        Assert.assertEquals(quiz, repo.findOne(quiz.id))
    }


}