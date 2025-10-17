package ru.razornd.phototransit.storage.file

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.nio.file.Path

@Component
@ConfigurationProperties(prefix = "file.storage.s3")
class S3FileStorage @Autowired constructor(private val s3Client: S3Client) : FileStorage {

    var bucket: String = "photo-origins"

    override fun saveFile(storagePath: String, file: Path) {
        val s3PutRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(storagePath)
            .build()

        s3Client.putObject(s3PutRequest, file)
    }

}
