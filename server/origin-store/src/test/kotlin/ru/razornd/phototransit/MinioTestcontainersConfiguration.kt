package ru.razornd.phototransit

import io.awspring.cloud.autoconfigure.core.AwsConnectionDetails
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MinIOContainer
import java.net.URI

@TestConfiguration(proxyBeanMethods = false)
class MinioTestcontainersConfiguration {
    @Bean
    fun minioTestcontainers(): MinIOContainer {
        return MinIOContainer("minio/minio:latest")
    }

    @Bean
    fun containerAwsConnectionDetails(container: MinIOContainer) = ContainerAwsConnectionDetails(container)

    class ContainerAwsConnectionDetails(private val container: MinIOContainer) : AwsConnectionDetails {

        override fun getEndpoint() = URI(container.s3URL)

        override fun getAccessKey(): String = container.userName

        override fun getSecretKey(): String = container.password

        override fun getRegion(): String = "test"
    }
}
