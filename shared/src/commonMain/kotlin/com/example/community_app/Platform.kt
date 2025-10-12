package com.example.community_app

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform