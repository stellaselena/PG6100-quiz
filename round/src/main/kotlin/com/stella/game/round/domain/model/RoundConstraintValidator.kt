package com.stella.game.round.domain.model

import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext

class RoundConstraintValidator : ConstraintValidator<RoundConstraint, Round> {

    override fun initialize(constraintAnnotation: RoundConstraint) {}

    override fun isValid(value: Round, context: ConstraintValidatorContext): Boolean {

        if ((value.winnerName == value.player1Username).xor(value.winnerName == value.player2Username))
            return true

        return false
    }
}