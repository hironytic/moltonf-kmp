package com.hironytic.moltonfkmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform