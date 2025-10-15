package ru.razornd.phototransit.service

import org.springframework.stereotype.Service
import ru.razornd.phototransit.PhotoId
import ru.razornd.phototransit.UserId
import java.io.InputStream
import java.time.Instant

enum class ImageFormat {
    JPEG,
    PNG,
    TIFF
}

enum class PhotoType {
    ORIGINAL,
    PROCESSED
}

data class UploadPhoto(
    val name: String,
    val inputStream: InputStream,
    val createdAt: Instant,
    val owner: UserId,
    val type: PhotoType
)

@Service
class UploadService {

    fun savePhoto(uploadPhoto: UploadPhoto, format: ImageFormat): PhotoId {
        TODO()
    }

}
