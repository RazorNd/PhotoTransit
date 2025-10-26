package ru.razornd.phototransit.rest

import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import ru.razornd.phototransit.PhotoId
import ru.razornd.phototransit.UserId
import ru.razornd.phototransit.service.PhotoType
import ru.razornd.phototransit.service.UploadPhoto
import ru.razornd.phototransit.service.UploadService
import ru.razornd.phototransit.web.AttachmentFilename
import java.io.InputStream
import java.util.*

private val SUPPORTED_MASTER_TYPES = setOf(MediaType.IMAGE_JPEG, MediaType.parseMediaType("image/tiff"))

@RestController
@RequestMapping("/api/origin-store/photos")
class UploadController(private val service: UploadService) {
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = ["image/*"])
    fun uploadPhoto(
        body: InputStream,
        @RequestParam("owner") owner: UUID,
        @RequestParam("type", defaultValue = "MASTER") type: PhotoType,
        @RequestHeader("Content-Type") contentType: MediaType,
        @AttachmentFilename filename: String
    ): UploadResult {
        if (type == PhotoType.MASTER && !SUPPORTED_MASTER_TYPES.isSupported(contentType)) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Unsupported media type '$contentType' for master photo"
            )
        }

        return UploadResult(service.savePhoto(UploadPhoto(filename, body, UserId(owner), type)))
    }

    private fun UploadResult(photoId: PhotoId) = UploadResult(photoId.id)

    private fun Collection<MediaType>.isSupported(contentType: MediaType) = any { it.includes(contentType) }
}
