plugins {
    id("org.jetbrains.kotlin.jvm")
    `java-library`
}


dependencies {
    api("org.springframework.boot:spring-boot-docker-compose")
    implementation(platform(libs.spring.boot.dependencies))
    implementation(platform(libs.spring.cloud.aws.dependencies))
    implementation("io.awspring.cloud:spring-cloud-aws-autoconfigure")
}

java {
    targetCompatibility = JavaVersion.VERSION_24
}

