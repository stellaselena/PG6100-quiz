package com.stella.game.category.repository

import com.stella.game.category.domain.model.Category
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

@Repository
interface CategoryRepository : CrudRepository<Category, Long>, CategoryRepositoryCustom {
    fun findByName(name: String): Long
}

@Transactional
interface CategoryRepositoryCustom {

    fun createCategory(name: String): Long
    fun update(id: Long, name: String): Boolean

}

@Repository
@Transactional
open class CategoryRepositoryImpl : CategoryRepositoryCustom {


    @PersistenceContext
    private lateinit var em: EntityManager

    override fun createCategory(name: String): Long {
        val entity = Category(name)
        em.persist(entity)
        return entity.id!!
    }

    override fun update(id: Long, name: String): Boolean {
        val category = em.find(Category::class.java, id) ?: return false
        category.name = name
        return true
    }

}