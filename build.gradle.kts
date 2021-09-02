import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.20"
    id("com.github.johnrengelman.shadow") version "2.0.4"
}

group = "me.vldf"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-server-core:1.6.1")
    implementation("io.ktor:ktor-server-netty:1.6.1")
    implementation("io.ktor:ktor-serialization:1.6.1")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    manifest.attributes.apply {
        //put("Implementation-Version" version)
        put("Main-Class", "me.vldf.server.MainKt")
    }
}