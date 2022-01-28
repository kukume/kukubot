package me.kuku.yuq.utils

import com.querydsl.core.BooleanBuilder
import com.querydsl.core.types.Predicate

operator fun BooleanBuilder.plus(right: Predicate) {
    this.and(right)
}

infix fun BooleanBuilder.and(right: Predicate) {
    this.and(right)
}