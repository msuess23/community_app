plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
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
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)
    implementation("io.ktor:ktor-server-host-common:${libs.versions.ktor.get()}")

    // --- JSON Serialization ---
    implementation("io.ktor:ktor-serialization-kotlinx-json:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-content-negotiation:${libs.versions.ktor.get()}")

    // --- Auth / JWT ---
    implementation("io.ktor:ktor-server-auth:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-auth-jwt:${libs.versions.ktor.get()}")
    implementation(libs.java.jwt)

    // --- Database (H2 + Exposed ORM)
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.h2)

    // --- File Uploads & Static Content
    implementation("io.ktor:ktor-server-cio:${libs.versions.ktor.get()}")

    // --- CORS, Compression, StatusPages ---
    implementation("io.ktor:ktor-server-cors:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-status-pages:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-call-logging:${libs.versions.ktor.get()}")

    // --- Logging ---
    implementation(libs.logback)

    // --- Testing ---
    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}