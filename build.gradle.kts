plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spring.boot) apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}


version = "0.0.1"
