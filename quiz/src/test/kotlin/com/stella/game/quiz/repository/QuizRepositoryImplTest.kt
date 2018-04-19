package com.stella.game.quiz.repository


import com.stella.game.quiz.domain.model.Quiz
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.junit4.SpringRunner



@RunWith(SpringRunner::class)
@DataJpaTest
class EntityRepositoryImplTest {


    @Autowired
    private lateinit var repo: QuizRepository

    @Before
    fun cleanDatabase(){
        repo.deleteAll()

    }

    @Test
    fun testInitialization() {
        Assert.assertNotNull(repo)
    }

    fun createQuiz():Quiz{
        val id =  repo.createQuiz("question", mutableListOf("a", "b", "c", "d"), 1, 1)
        return repo.findOne(id)
    }

    fun createSomeQuizzes(): Iterable<Quiz>{
        val id1 =  repo.createQuiz("question", mutableListOf("a", "b", "c", "d"), 1, 1)
        val id2 =  repo.createQuiz("question", mutableListOf("a", "b", "c", "d"), 1, 1)
        val id3 =  repo.createQuiz("question", mutableListOf("a", "b", "c", "d"), 1, 2)
        val id4 =  repo.createQuiz("question", mutableListOf("a", "b", "c", "d"), 1, 3)
        val id5 =  repo.createQuiz("question", mutableListOf("a", "b", "c", "d"), 1, 4)
        val quizIds = listOf(id1, id2, id3, id4, id5)
        val quizzes = listOf<Quiz>().toMutableList()
        for(i in quizIds){
            quizzes.add(repo.findOne(i))
        }
        return quizzes
    }

    @Test
    fun testCreateQuiz(){


        val quiz = createQuiz()
        Assert.assertEquals(1, repo.count())
        Assert.assertEquals(quiz, repo.findOne(quiz.id))
    }


    @Test
    fun testFindAllQuizzesInSubcategory(){

        createSomeQuizzes()
        Assert.assertEquals(2, repo.findAllBySubcategoryId(1L).count())
    }

    @Test
    fun testFindRandomQuizWithSubcategory(){
        createSomeQuizzes()

        val quizzes = repo.findAll()
        val random = repo.findRandomQuizWithSubcategory(quizzes, 3L)
        var checkRandom = false
        if(random?.subcategoryId == 3L){
            checkRandom = true
        }
        Assert.assertTrue(checkRandom)


    }


}
