
plugins {
    kotlin("jvm") version "1.5.20"
    id("maven-publish")
}

group = "org.skyne.logging"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.20")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.20")
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("ch.qos.logback:logback-core:1.2.3")
    implementation("com.google.code.gson:gson:2.8.7")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.github.skyne"
            artifactId = "bunyanlayout"
            version = "0.0.2"

            from(components["java"])
        }
    }
}

