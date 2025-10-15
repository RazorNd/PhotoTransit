import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

group = "ru.razornd.phototransit"

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.springframework.boot")


    val implementation by configurations
    val testImplementation by configurations
    val testRuntimeOnly by configurations

    val mockitoAgent by configurations.creating

    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.springframework.boot:spring-boot-starter-actuator")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.springframework.boot:spring-boot-testcontainers")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")

        mockitoAgent("org.mockito:mockito-core") { isTransitive = false }
    }

    extensions.configure<JavaPluginExtension> {
        targetCompatibility = JavaVersion.VERSION_24
    }

    extensions.configure<KotlinJvmProjectExtension> {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_24
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        jvmArgs("-javaagent:${mockitoAgent.asPath}")
    }
}
