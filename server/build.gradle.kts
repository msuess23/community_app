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
    // --- Shared ---
    implementation(projects.shared)

    // --- Ktor ---
    implementation(libs.bundles.ktor.server)
    implementation(libs.ktor.serialization.kotlinx.json)

    // --- Security ---
    implementation(libs.java.jwt)
    implementation(libs.argon2)

    // --- Database (H2 + Exposed ORM)
    implementation(libs.bundles.exposed)
    implementation(libs.h2)

    // --- Logging ---
    implementation(libs.logback)

    // --- Testing ---
    testImplementation(libs.kotlin.testJunit)
}