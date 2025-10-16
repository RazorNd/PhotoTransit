package ru.razornd.phototransit.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import ru.razornd.phototransit.PhotoId
import ru.razornd.phototransit.UserId
import ru.razornd.phototransit.model.Photo
import ru.razornd.phototransit.model.PhotoFile
import ru.razornd.phototransit.storage.PhotoRepository
import ru.razornd.phototransit.storage.file.FileStorage
import ru.razornd.phototransit.util.TemporaryFileProcessor
import java.io.ByteArrayInputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.util.*
import kotlin.random.Random

@SpringBootTest(classes = [DefaultUploadService::class, TemporaryFileProcessor::class])
class DefaultUploadServiceTest {

    @MockitoBean
    lateinit var repository: PhotoRepository

    @MockitoBean
    lateinit var fileStorage: FileStorage

    @Autowired
    lateinit var service: DefaultUploadService

    val photoArgument = argumentCaptor<Photo.New>()
    val photoFileArgument = argumentCaptor<PhotoFile.New>()
    val pathArgument = argumentCaptor<Path>()
    val bytes = Random.nextBytes(512)

    val uploadPhoto = UploadPhoto(
        "test-image",
        ByteArrayInputStream(bytes),
        Instant.parse("2016-04-10T22:02:31Z"),
        UserId("b93d879c-ba90-48df-95fd-f96490d11755"),
        PhotoType.ORIGINAL
    )
    val storagePath = "${uploadPhoto.owner.id}/${uploadPhoto.name}.tiff"

    @Test
    fun savePhoto(@TempDir tempDir: Path) {
        val savedFile = Files.createTempFile(tempDir, "test-image", ".tiff")

        repository.stub {
            on { upsert(any()) } doAnswer copyFieldWithId(PhotoId("32fd328f-e767-4f6c-86d9-8c5c8769a4e6"))
            on { saveFile(any()) } doAnswer copyFieldWithId(UUID.fromString("99937004-317a-4e8c-bca5-c268909bcd41"))
        }
        fileStorage.stub {
            on { saveFile(any(), any()) } doAnswer { (_: Any, path: Path) ->
                Files.copy(path, savedFile, StandardCopyOption.REPLACE_EXISTING)
                Unit
            }
        }

        service.savePhoto(uploadPhoto, ImageFormat.TIFF)

        verify(repository).upsert(photoArgument.capture())
        verify(repository).saveFile(photoFileArgument.capture())
        verify(fileStorage).saveFile(eq(storagePath), pathArgument.capture())

        assertThat(photoArgument.singleValue)
            .describedAs("Save photo")
            .usingRecursiveComparison()
            .isEqualTo(
                Photo.New(
                    owner = uploadPhoto.owner,
                    name = uploadPhoto.name,
                    createdAt = uploadPhoto.createdAt
                )
            )

        assertThat(photoFileArgument.singleValue)
            .describedAs("Save photo file")
            .usingRecursiveComparison()
            .isEqualTo(
                PhotoFile.New(
                    photoId = PhotoId("32fd328f-e767-4f6c-86d9-8c5c8769a4e6"),
                    type = uploadPhoto.type,
                    storagePath = storagePath
                )
            )

        assertThat(savedFile)
            .describedAs("Saved file content")
            .hasBinaryContent(bytes)

        assertThat(pathArgument.singleValue)
            .describedAs("Temporary file should be deleted")
            .doesNotExist()
    }

    private fun copyFieldWithId(fileId: UUID): (KInvocationOnMock) -> PhotoFile = { (new: PhotoFile.New) ->
        PhotoFile(
            id = fileId,
            photoId = new.photoId,
            type = new.type,
            storagePath = new.storagePath
        )
    }

    private fun copyFieldWithId(photoId: PhotoId): (KInvocationOnMock) -> Photo = { (new: Photo.New) ->
        Photo(
            id = photoId,
            owner = new.owner,
            name = new.name,
            createdAt = new.createdAt
        )
    }
}
