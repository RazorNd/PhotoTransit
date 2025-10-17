dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    implementation(platform(libs.spring.cloud.aws.dependencies))
    implementation("io.awspring.cloud:spring-cloud-aws-starter-s3")

    runtimeOnly("org.flywaydb:flyway-core")
    runtimeOnly("org.flywaydb:flyway-database-postgresql")
    runtimeOnly("org.postgresql:postgresql")

    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    testImplementation("org.springframework:spring-webflux")
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.assertj.db)
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")
    testImplementation(libs.testcontainers.minio)
}
