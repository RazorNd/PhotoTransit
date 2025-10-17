package ru.razornd.phototransit

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import

@Import(PostgresTestcontainersConfiguration::class, MinioTestcontainersConfiguration::class)
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration
