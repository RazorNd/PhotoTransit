plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.graavm.buldtools.native) apply false
}

allprojects {
    version = "0.0.1"
    repositories {
        mavenCentral()
    }
}

val copyScreenshoots = tasks.register<Copy>("copyScreenshots") {
    from(subprojects.map { it.layout.buildDirectory.dir("reports/screenshots") })
    into(layout.buildDirectory.dir("reports/screenshots"))
}
