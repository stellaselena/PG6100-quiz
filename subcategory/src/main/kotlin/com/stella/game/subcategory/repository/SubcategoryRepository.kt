package com.stella.game.subcategory.repository

import com.fasterxml.jackson.annotation.JsonBackReference
import com.stella.game.subcategory.domain.model.Subcategory
import org.hibernate.validator.constraints.NotBlank
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import javax.persistence.*
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Repository
interface SubcategoryRepository : CrudRepository<Subcategory, Long>, SubcategoryRepositoryCustom {
        fun findAllByCategory(category: Long): Iterable<Subcategory>


}

@Transactional
interface SubcategoryRepositoryCustom {

        fun createSubcategory(name: String, category: Long): Long
        fun update(id: Long, name: String, category: Long): Boolean
}

@Repository
@Transactional
open class SubcategoryRepositoryImpl : SubcategoryRepositoryCustom {

        @PersistenceContext
        private lateinit var em: EntityManager

        override fun createSubcategory(name: String, category: Long): Long {
                val entity = Subcategory(name, category)
                em.persist(entity)

                return entity.id!!
        }

        override fun update(id: Long, name: String, category: Long): Boolean {
                val subcategory = em.find(Subcategory::class.java, id) ?: return false
                subcategory.name = name
                subcategory.category = category
                return true
        }

}