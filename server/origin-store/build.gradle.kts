dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    testImplementation("org.springframework:spring-webflux")
    testImplementation(libs.mockito.kotlin)
}
