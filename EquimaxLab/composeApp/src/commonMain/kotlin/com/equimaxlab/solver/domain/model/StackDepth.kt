package com.equimaxlab.solver.domain.model

enum class StackDepth {
    SHORT,    // < 20bb
    MEDIUM,   // 20-40bb
    DEEP,     // 40-100bb
    VERY_DEEP // > 100bb
}