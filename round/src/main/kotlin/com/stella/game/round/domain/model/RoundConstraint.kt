package com.stella.game.round.domain.model

import com.stella.game.round.RoundApplicationConfig
import java.lang.annotation.Documented
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@Constraint(validatedBy = arrayOf(RoundConstraintValidator::class))
@Target(AnnotationTarget.CLASS, AnnotationTarget.FILE, AnnotationTarget.ANNOTATION_CLASS)
@Retention(value = AnnotationRetention.RUNTIME)
@Documented
annotation class RoundConstraint(
        val message: String = "Invalid constraints in Round state",
        val groups: Array<KClass<*>> = arrayOf(),
        val payload: Array<KClass<out Payload>> = arrayOf())

