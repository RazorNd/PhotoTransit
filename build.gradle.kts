plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spring.boot) apply false
}

allprojects {
    version = "0.0.1"
    repositories {
        mavenCentral()
    }
}
