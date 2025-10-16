package ru.razornd.phototransit.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.razornd.phototransit.PhotoId
import ru.razornd.phototransit.UserId
import ru.razornd.phototransit.model.Photo
import ru.razornd.phototransit.model.PhotoFile
import ru.razornd.phototransit.storage.PhotoRepository
import ru.razornd.phototransit.storage.file.FileStorage
import ru.razornd.phototransit.util.TemporaryFileProcessor
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

interface UploadService {

    fun savePhoto(uploadPhoto: UploadPhoto, format: ImageFormat): PhotoId

}

@Service
open class DefaultUploadService(
    private val photoRepository: PhotoRepository,
    private val fileStorage: FileStorage,
    private val fileProcessor: TemporaryFileProcessor
) : UploadService {

    @Transactional
    override fun savePhoto(uploadPhoto: UploadPhoto, format: ImageFormat): PhotoId {
        val extension = format.name.lowercase()
        val storagePath = "${uploadPhoto.owner.id}/${uploadPhoto.name}.$extension"

        val photo = photoRepository.upsert(Photo.New(uploadPhoto.owner, uploadPhoto.name, uploadPhoto.createdAt))
        photoRepository.saveFile(PhotoFile.New(photo.id, uploadPhoto.type, storagePath))

        fileProcessor.process(uploadPhoto.inputStream, uploadPhoto.name, ".$extension") {
            fileStorage.saveFile(storagePath, it)
        }

        return photo.id
    }

}
