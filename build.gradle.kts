import java.util.*

plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "br.com.finalcraft"
version = "2.0.1"

repositories {
    mavenCentral()

    maven {
        name = "papermc"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }

    maven {
        name = "aikar-repo"
        url = uri("https://repo.aikar.co/content/groups/aikar/")
    }

    maven {
        name = "proxi-nexus"
        url = uri("https://nexus.proximyst.com/repository/maven-public/")
    }

    maven {
        name = "dmulloy2-repo"
        url = uri("https://repo.dmulloy2.net/nexus/repository/public/")
    }
    maven {
        name = "petrus-repo"
        url = uri("https://maven.petrus.dev/public")
    }
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.15-R0.1-SNAPSHOT")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")
    compileOnly("org.jetbrains:annotations:19.0.0")
    compileOnly("org.apache.logging.log4j:log4j-api:2.13.2")
    compileOnly("org.apache.logging.log4j:log4j-core:2.13.2")
    compileOnly("br.com.finalcraft:EverNifeCore:2.0.4")

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = sourceCompatibility
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

publishing.publications.create<MavenPublication> ("mavenJava") {
    from(components["java"])
}
