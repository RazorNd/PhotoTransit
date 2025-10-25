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

tasks.register<JavaExec>("playwrightInstall") {
    group = "verification"
    description = "Install Playwright browsers"
    classpath = configurations.detachedConfiguration(
        dependencies.create(libs.playwright.get())
    )
    mainClass.set("com.microsoft.playwright.CLI")
    args = listOf("install", "--with-deps")
}

tasks.register<Copy>("copyScreenshots") {
    from(subprojects.map { it.layout.buildDirectory.dir("reports/screenshots") })
    into(layout.buildDirectory.dir("reports/screenshots"))
}

tasks.register<Copy>("copyVideos") {
    from(subprojects.map { it.layout.buildDirectory.dir("reports/videos") })
    into(layout.buildDirectory.dir("reports/videos"))
}
