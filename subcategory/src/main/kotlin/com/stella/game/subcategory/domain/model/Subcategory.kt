package com.stella.game.subcategory.domain.model

import com.fasterxml.jackson.annotation.JsonBackReference
import org.hibernate.validator.constraints.NotBlank
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@Entity
data class Subcategory (
        @get:NotBlank
        @get:Size(max = 32)
        var name: String,

//        @get:ManyToOne(fetch = FetchType.EAGER)
        @get:NotNull
        @JsonBackReference
        var category: Long,

        @get:Id
        @get:GeneratedValue
        var id: Long? = null
)