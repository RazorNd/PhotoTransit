package ru.razornd.docker.compose.service.connection

import io.awspring.cloud.autoconfigure.core.AwsConnectionDetails
import org.springframework.boot.docker.compose.core.RunningService
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionDetailsFactory
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionSource
import java.net.URI

internal class MinioAwsDockerConnectionFactory :
    DockerComposeConnectionDetailsFactory<AwsConnectionDetails>("minio/minio") {

    override fun getDockerComposeConnectionDetails(source: DockerComposeConnectionSource): AwsConnectionDetails {
        return MinioAwsDockerConnectionDetails(source.runningService)
    }

    class MinioAwsDockerConnectionDetails(private val runningService: RunningService) : AwsConnectionDetails {
        override fun getEndpoint() = URI("http://${runningService.host()}:${runningService.ports().get(9000)}")

        override fun getRegion() = "local"

        override fun getAccessKey() = runningService.env()["MINIO_ROOT_USER"] ?: "minioadmin"

        override fun getSecretKey() = runningService.env()["MINIO_ROOT_PASSWORD"] ?: "minioadmin"
    }
}
