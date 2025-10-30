package ru.razornd.phototransit.image

import com.ashampoo.kim.Kim
import com.ashampoo.kim.jvm.readMetadata
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.time.Instant


@Component
class KimImageMetadata : ImageMetadataReader {

    override fun readMetaData(path: Path): KimImageMetadata {
        val metadata = requireNotNull(Kim.readMetadata(path)) { "Metadata not found in $path" }

        return KimImageMetadata(requireNotNull(metadata.originalDateTime()) { "Original date not found in $path" })
    }

    data class KimImageMetadata(override val createDate: Instant) : ImageMetadata
}
