plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.20"
    id("org.jetbrains.intellij") version "1.17.0"
}

group = "com.dekonoplyov"
version = "0.3.0-germanKeyboardFix"

repositories {
    mavenCentral()
}

intellij {
    version.set("2024.3.3")
    type.set("IC") // Target IDE Platform
    plugins.set(listOf())
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("243")
    }

    publishPlugin {
        token.set(System.getenv("ORG_GRADLE_PROJECT_intellijPublishToken"))
    }
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.9.20")
}
