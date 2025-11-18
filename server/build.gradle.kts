plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
    application
}

group = "com.example.community_app"
version = "1.0.0"
application {
    mainClass.set("com.example.community_app.ApplicationKt")

//    val isDevelopment: Boolean = project.ext.has("development")
//    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    // --- shared ---
    implementation(projects.shared)

    // --- Ktor Core ---
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.hostCommon)

    // --- JSON Serialization ---
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.contentNegotiation)

    // --- Auth / JWT ---
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.authJwt)
    implementation(libs.java.jwt)

    // --- Password Hashing (Argon2)
    implementation(libs.argon2)


    // --- Database (H2 + Exposed ORM)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)
    implementation(libs.h2)

    // --- File Uploads & Static Content
    implementation(libs.ktor.server.cio)

    // --- CORS, Compression, StatusPages ---
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.statusPages)
    implementation(libs.ktor.server.callLogging)

    // --- Logging ---
    implementation(libs.logback)

    // --- Testing ---
    testImplementation(libs.ktor.server.testHost)
    testImplementation(libs.kotlin.testJunit)
}