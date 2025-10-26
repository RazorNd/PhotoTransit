package ru.razornd.phototransit.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.razornd.phototransit.PhotoId
import ru.razornd.phototransit.UserId
import ru.razornd.phototransit.image.ImageMetadataReader
import ru.razornd.phototransit.model.Photo
import ru.razornd.phototransit.model.PhotoFile
import ru.razornd.phototransit.storage.PhotoRepository
import ru.razornd.phototransit.storage.file.FileStorage
import ru.razornd.phototransit.util.TemporaryFileProcessor
import java.io.InputStream

enum class PhotoType {
    ORIGINAL,
    MASTER,
}

data class UploadPhoto(
    val filename: String,
    val inputStream: InputStream,
    val owner: UserId,
    val type: PhotoType
)

interface UploadService {

    fun savePhoto(uploadPhoto: UploadPhoto): PhotoId

}

@Service
open class DefaultUploadService(
    private val photoRepository: PhotoRepository,
    private val fileStorage: FileStorage,
    private val fileProcessor: TemporaryFileProcessor,
    private val metaDataReader: ImageMetadataReader
) : UploadService {

    @Transactional
    override fun savePhoto(uploadPhoto: UploadPhoto): PhotoId {
        val storagePath = "${uploadPhoto.owner.id}/${uploadPhoto.filename}"

        lateinit var photo: Photo

        fileProcessor.process(uploadPhoto.inputStream, uploadPhoto.filename, ".${uploadPhoto.fileExtension}") {
            val metaData = metaDataReader.readMetaData(it)

            photo = photoRepository.upsert(
                Photo.New(
                    uploadPhoto.owner,
                    uploadPhoto.name,
                    metaData.createDate
                )
            )
            photoRepository.saveFile(PhotoFile.New(photo.id, uploadPhoto.type, storagePath))

            fileStorage.saveFile(storagePath, it)
        }

        return photo.id
    }

    private val UploadPhoto.fileExtension get() = filename.substringAfterLast('.', ".bin")

    private val UploadPhoto.name get() = filename.substringBeforeLast('.')

}
