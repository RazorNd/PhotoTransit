package ru.razornd.phototransit.rest

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import ru.razornd.phototransit.PhotoId
import ru.razornd.phototransit.UserId
import ru.razornd.phototransit.service.ImageFormat
import ru.razornd.phototransit.service.PhotoType
import ru.razornd.phototransit.service.UploadPhoto
import ru.razornd.phototransit.service.UploadService
import java.io.InputStream
import java.time.Instant
import java.util.*

private val IMAGE_TIFF = MediaType.parseMediaType("image/tiff")

@RestController
@RequestMapping("/api/origin-store/photos")
class UploadController(private val service: UploadService) {

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = ["image/jpeg", "image/png", "image/tiff"])
    fun uploadPhoto(
        body: InputStream,
        @RequestParam("name") name: String,
        @RequestParam("owner") owner: UUID,
        @RequestParam("created_at") createdAt: Instant,
        @RequestParam("type", defaultValue = "PROCESSED") type: PhotoType,
        @RequestHeader("Content-Type") contentType: MediaType,
    ): UploadResult {
        val dto = UploadPhoto(
            name,
            body,
            createdAt,
            UserId(owner),
            type
        )

        val photoId = service.savePhoto(dto, contentType.toImageFormat())

        return UploadResult(photoId)
    }

    private fun UploadResult(photoId: PhotoId) = UploadResult(photoId.id)

    private fun MediaType.toImageFormat(): ImageFormat {
        return when {
            isCompatibleWith(MediaType.IMAGE_JPEG) -> ImageFormat.JPEG
            isCompatibleWith(MediaType.IMAGE_PNG) -> ImageFormat.PNG
            isCompatibleWith(IMAGE_TIFF) -> ImageFormat.TIFF
            else -> error("Unsupported content type '${this}'")
        }
    }

}
