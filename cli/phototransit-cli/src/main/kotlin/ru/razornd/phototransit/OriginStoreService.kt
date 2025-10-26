package ru.razornd.phototransit

import org.springframework.core.io.PathResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import ru.razornd.phototransit.http.OriginStoreClient
import ru.razornd.phototransit.http.OriginStoreClient.PhotoType.MASTER
import ru.razornd.phototransit.http.OriginStoreClient.PhotoType.ORIGINAL
import java.nio.file.Path
import java.util.*
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.notExists

sealed class CliApplicationException(message: String) : RuntimeException(message)

class UnsupportedFileTypeException(message: String) : CliApplicationException(message)

class FileNotExistsException(message: String) : CliApplicationException(message)

private val IMAGE_MEDIA_TYPE = MediaType.parseMediaType("image/*")

@Service
class OriginStoreService(
    private val client: OriginStoreClient,
    private val mediaTypeResolver: MediaTypeResolver
) {

    @Throws(FileNotExistsException::class, UnsupportedFileTypeException::class)
    fun uploadPhoto(file: String, original: Boolean): UUID {
        val path = Path.of(file)

        if (path.notExists()) throw FileNotExistsException("File '$file' not exists")
        if (path.isDirectory()) throw FileNotExistsException("'$file' is are directory")

        val mediaType =
            mediaTypeResolver.resolve(path) ?: throw UnsupportedFileTypeException("Unknown file type for '$file'")

        if (!mediaType.isImage) throw UnsupportedFileTypeException("Unsupported file type for '$file': $mediaType")

        val uploadPhoto = client.uploadPhoto(
            PathResource(path),
            path.name,
            if (original) ORIGINAL else MASTER,
            mediaType
        )

        return uploadPhoto.id
    }

    private val MediaType.isImage get() = isCompatibleWith(IMAGE_MEDIA_TYPE)

}
