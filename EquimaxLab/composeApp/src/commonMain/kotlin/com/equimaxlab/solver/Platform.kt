package com.equimaxlab.solver

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform