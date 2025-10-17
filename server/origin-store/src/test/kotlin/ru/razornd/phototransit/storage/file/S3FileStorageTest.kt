package ru.razornd.phototransit.storage.file

import io.awspring.cloud.autoconfigure.core.AwsAutoConfiguration
import io.awspring.cloud.autoconfigure.core.CredentialsProviderAutoConfiguration
import io.awspring.cloud.autoconfigure.core.RegionProviderAutoConfiguration
import io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration
import io.awspring.cloud.s3.S3Operations
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import ru.razornd.phototransit.MinioTestcontainersConfiguration
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.writeText

@ImportAutoConfiguration(
    CredentialsProviderAutoConfiguration::class,
    RegionProviderAutoConfiguration::class,
    AwsAutoConfiguration::class,
    S3AutoConfiguration::class
)
@Import(MinioTestcontainersConfiguration::class)
@SpringBootTest(
    classes = [S3FileStorage::class],
    properties = [
        "spring.cloud.aws.s3.path-style-access-enabled=true",
        "file.storage.s3.bucket=test-bucket",
    ]
)
class S3FileStorageTest {

    @Autowired
    lateinit var storage: S3FileStorage

    @BeforeEach
    fun setUp(@Autowired s3operations: S3Operations) {
        s3operations.createBucket("test-bucket")
    }

    @Test
    fun saveFile(@TempDir tempDir: Path, @Autowired s3Client: S3Client) {
        val uploadedFile = Files.createTempFile(tempDir, "test", ".bin")

        uploadedFile.writeText("test file content")

        storage.saveFile("test.bin", uploadedFile)

        val s3Response = s3Client.getObject(GetObjectRequest.builder().bucket("test-bucket").key("test.bin").build())

        assertThat(s3Response).hasContent("test file content")
    }
}
