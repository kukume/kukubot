package me.kuku.yuq.utils

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.BooleanExpression

operator fun BooleanBuilder.plus(right: Predicate) {
    this.and(right)
}

infix fun BooleanBuilder.and(right: Predicate) {
    this.and(right)
}

operator fun BooleanExpression.plus(right: Predicate): BooleanExpression {
    return this.and(right)
}