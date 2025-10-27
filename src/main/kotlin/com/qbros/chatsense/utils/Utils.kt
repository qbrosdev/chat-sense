package com.qbros.chatsense.utils

import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.system.measureTimeMillis

inline fun <T> measure(block: () -> T): Pair<T, Long> {
    val value: T
    val duration = measureTimeMillis {
        value = block()
    }
    return value to duration
}

object JsonUtil {
    val mapper: ObjectMapper = ObjectMapper()
}