package com.stella.game.subcategory.domain.converter

import com.stella.game.schema.SubcategoryDto
import com.stella.game.subcategory.domain.model.Subcategory

class SubcategoryConverter {

    companion object {

        fun transform(entity: Subcategory): SubcategoryDto {

            return SubcategoryDto(
                    id = entity.id?.toString(),
                    name = entity.name,
                    category = entity.category

            ).apply {
                id = entity.id?.toString()
            }
        }

        fun transform(entities: Iterable<Subcategory>): List<SubcategoryDto> {
            return entities.map { transform(it) }
        }
    }
}