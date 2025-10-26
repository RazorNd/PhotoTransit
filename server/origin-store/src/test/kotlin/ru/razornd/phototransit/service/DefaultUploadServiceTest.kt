package ru.razornd.phototransit.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import ru.razornd.phototransit.PhotoId
import ru.razornd.phototransit.UserId
import ru.razornd.phototransit.image.ImageMetadata
import ru.razornd.phototransit.image.ImageMetadataReader
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
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.random.Random

@SpringBootTest(classes = [DefaultUploadService::class, TemporaryFileProcessor::class])
class DefaultUploadServiceTest {

    @MockitoBean
    lateinit var repository: PhotoRepository

    @MockitoBean
    lateinit var fileStorage: FileStorage

    @MockitoBean
    lateinit var metaDataReader: ImageMetadataReader

    @Autowired
    lateinit var service: DefaultUploadService

    private val photoArgument = argumentCaptor<Photo.New>()
    private val photoFileArgument = argumentCaptor<PhotoFile.New>()
    private val pathArgument = argumentCaptor<Path>()
    private val bytes = Random.nextBytes(512)

    private val uploadPhoto = UploadPhoto(
        "test-image.tiff",
        ByteArrayInputStream(bytes),
        UserId("b93d879c-ba90-48df-95fd-f96490d11755"),
        PhotoType.MASTER
    )

    private val storagePath = "${uploadPhoto.owner.id}/${uploadPhoto.filename}"

    private lateinit var savedFile: Path

    @BeforeEach
    fun setUp(@TempDir tempDir: Path) {
        repository.stub {
            on { upsert(any()) } doAnswer copyWithId(PhotoId("32fd328f-e767-4f6c-86d9-8c5c8769a4e6"))
            on { saveFile(any()) } doAnswer copyWithId(UUID.fromString("99937004-317a-4e8c-bca5-c268909bcd41"))
        }
        fileStorage.stub {
            on { saveFile(any(), any()) } doAnswer { (_: Any, path: Path) ->
                Files.copy(path, savedFile, StandardCopyOption.REPLACE_EXISTING).let { }
            }
        }
        metaDataReader.stub {
            on { readMetaData(any()) } doReturn TestImageMetadata
        }
        savedFile = createTempFile(tempDir, "test-image", ".tiff")
    }

    @AfterEach
    fun tearDown() {
        savedFile.deleteIfExists()
    }

    @Test
    fun savePhoto() {
        service.savePhoto(uploadPhoto)

        verify(repository).upsert(photoArgument.capture())
        verify(repository).saveFile(photoFileArgument.capture())
        verify(fileStorage).saveFile(eq(storagePath), pathArgument.capture())

        assertThat(photoArgument.singleValue)
            .describedAs("Save photo")
            .usingRecursiveComparison()
            .isEqualTo(
                Photo.New(
                    owner = uploadPhoto.owner,
                    name = "test-image",
                    createdAt = TestImageMetadata.createDate
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

    private fun copyWithId(fileId: UUID): (KInvocationOnMock) -> PhotoFile = { (new: PhotoFile.New) ->
        PhotoFile(
            id = fileId,
            photoId = new.photoId,
            type = new.type,
            storagePath = new.storagePath
        )
    }

    private fun copyWithId(photoId: PhotoId): (KInvocationOnMock) -> Photo = { (new: Photo.New) ->
        Photo(
            id = photoId,
            owner = new.owner,
            name = new.name,
            createdAt = new.createdAt
        )
    }

    private object TestImageMetadata : ImageMetadata {
        override val createDate: Instant = Instant.parse("1995-09-28T07:53:24Z")
    }
}
