package com.stella.game.quiz.repository

import com.stella.game.quiz.domain.model.Quiz
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Repository
interface QuizRepository : CrudRepository<Quiz, Long>, QuizRepositoryCustom {

    fun findAllBySubcategoryId(subcategoryId: Long): Iterable<Quiz>


}

@Transactional
interface QuizRepositoryCustom {

    fun createQuiz(question: String, answers: MutableList<String>, correctAnswer: Int, subcategoryId: Long): Long

    fun updateQuestion(id: Long, question: String): Boolean

    fun update(id: Long, question: String, answers: MutableList<String>, correctAnswer: Int, subcategoryId: Long): Boolean

    fun findRandomQuizWithSubcategory(quizzes: Iterable<Quiz>, subcategoryId: Long): Quiz?

    fun findARandomQuiz(quizzes: Iterable<Quiz>) : Quiz

}

@Repository
@Transactional
open class QuizRepositoryImpl : QuizRepositoryCustom {

    @PersistenceContext
    private lateinit var em: EntityManager

    override fun createQuiz(question: String, answers: MutableList<String>, correctAnswer: Int, subcategoryId: Long): Long {
        val entity = Quiz(question, answers, correctAnswer, subcategoryId)
        em.persist(entity)
        return entity.id!!
    }

    override fun updateQuestion(id: Long, question: String): Boolean {
        val quiz = em.find(Quiz::class.java, id) ?: return false
        quiz.question = question
        return true
    }

    override fun update(id: Long, question: String, answers:MutableList<String>, correctAnswer: Int, subcategoryId: Long): Boolean {
        val quiz = em.find(Quiz::class.java, id) ?: return false
        quiz.question = question
        quiz.answers = answers
        quiz.correctAnswer = correctAnswer
        quiz.subcategoryId = subcategoryId
        return true
    }

    override fun findRandomQuizWithSubcategory(quizzes:Iterable<Quiz>, subcategoryId: Long): Quiz? {
        try {
            val result =  quizzes.filter { quizEntity -> quizEntity.subcategoryId == subcategoryId }
            if (result.count() == 0){
                throw IllegalArgumentException("No subcategories are found")
            }
            return rand(result)

        } catch (e: Exception){
            throw IllegalArgumentException("Subcategory not found")
        }

    }

    override fun findARandomQuiz(quizzes: Iterable<Quiz>): Quiz {
        return rand(quizzes.toList())
    }


    fun rand(quizzes : List<Quiz>) : Quiz {
        val quizSize = quizzes.count()

        if(quizSize == 1){
            return quizzes.first()
        } else return if (quizSize == 0){
            throw IllegalArgumentException("No quizzes are found")
        } else {
            val random = Random()
            val i = random.nextInt(quizSize)
            quizzes[i]
        }
    }

}