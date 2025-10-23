import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("org.graalvm.buildtools.native")
}

val mockitoAgent by configurations.creating

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.springframework.boot:spring-boot-starter-json")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.shell:spring-shell-starter")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation(platform(libs.spring.shell.dependencies))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.shell:spring-shell-test")
    testImplementation("org.springframework.shell:spring-shell-test-autoconfigure")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.wiremock.integration.spring)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
}

java {
    sourceCompatibility = JavaVersion.VERSION_24
    toolchain { languageVersion = JavaLanguageVersion.of(25) }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
        jvmTarget = JvmTarget.JVM_24
    }
}

graalvmNative {
    binaries {
        all {
            javaLauncher = javaToolchains.launcherFor { languageVersion = JavaLanguageVersion.of(25) }
        }
    }
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-javaagent:${mockitoAgent.asPath}")
}

tasks.bootBuildImage {
    enabled = false
}

tasks.bootJar {
    launchScript()
}
