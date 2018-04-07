package com.stella.game.category.domain.converter

import com.stella.game.category.domain.model.Category
import com.stella.game.schema.CategoryDto


class CategoryConverter {

    companion object {

        fun transform(entity: Category): CategoryDto {

            return CategoryDto(
                    id = entity.id?.toString(),
                    name = entity.name
            ).apply {
                id = entity.id?.toString()
            }
        }

        fun transform(entities: Iterable<Category>): List<CategoryDto> {
            return entities.map { transform(it) }
        }
    }
}