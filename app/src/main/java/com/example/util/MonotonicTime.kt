package com.example.util

import java.util.concurrent.atomic.AtomicLong

object MonotonicTime {
    private val lastTime = AtomicLong(0L)

    fun now(): Long {
        while (true) {
            val current = System.currentTimeMillis()
            val last = lastTime.get()
            val next = if (current > last) current else last + 1
            if (lastTime.compareAndSet(last, next)) {
                return next
            }
        }
    }
}
