package ru.razornd.phototransit.image

import com.ashampoo.kim.Kim
import com.ashampoo.kim.common.convertToPhotoMetadata
import com.ashampoo.kim.jvm.readMetadata
import com.ashampoo.kim.model.PhotoMetadata
import org.springframework.stereotype.Component
import java.nio.file.Path
import java.time.Instant

@Component
class KimImageMetadata : ImageMetadataReader {

    override fun readMetaData(path: Path) = KimImageMetadata(
        checkNotNull(Kim.readMetadata(path)) { "Metadata not found for $path" }.convertToPhotoMetadata()
    )

    class KimImageMetadata(photoMetadata: PhotoMetadata) : ImageMetadata {
        override val createDate: Instant = Instant.ofEpochMilli(
            requireNotNull(photoMetadata.takenDate) { "Photo metadata taken date cannot be null" }
        )
    }

}
