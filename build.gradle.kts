plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spring.boot) apply false
}

subprojects {
    repositories {
        mavenCentral()
    }
}


version = "0.0.1"
