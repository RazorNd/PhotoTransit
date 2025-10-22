package ru.razornd.phototransit

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.PathResource
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import ru.razornd.phototransit.http.OriginStoreClient
import ru.razornd.phototransit.http.OriginStoreClient.UploadResult
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Instant
import java.util.*
import kotlin.io.path.createTempFile
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.setAttribute

@SpringBootTest(classes = [OriginStoreService::class])
class OriginStoreServiceTest {

    @MockitoBean
    lateinit var client: OriginStoreClient

    @MockitoBean
    lateinit var mediaTypeResolver: MediaTypeResolver

    @Autowired
    lateinit var service: OriginStoreService

    @Test
    fun uploadPhoto(@TempDir tempDir: Path) {
        val testFile = createTempFile(tempDir, "test-image", ".jpg")
        val uploadResult = UploadResult(UUID.fromString("3b3831ba-02d1-4284-b2f7-7e8c74c6ccc6"))
        val createdAt = Instant.parse("2025-01-12T13:43:00Z")

        testFile.setAttribute("creationTime", FileTime.from(createdAt))

        mediaTypeResolver.stub {
            on { resolve(any()) } doReturn MediaType.IMAGE_JPEG
        }
        client.stub {
            on { uploadPhoto(any(), any(), any(), any(), any()) } doReturn uploadResult
        }

        val actual = service.uploadPhoto(testFile.toString(), true)

        assertThat(actual).isEqualTo(uploadResult.id)

        verify(client)
            .uploadPhoto(
                eq(PathResource(testFile)),
                eq(testFile.nameWithoutExtension),
                eq(createdAt),
                eq(OriginStoreClient.PhotoType.ORIGINAL),
                eq(MediaType.IMAGE_JPEG)
            )
    }

    @Test
    fun `uploadPhoto file not exists`(@TempDir tempDir: Path) {
        val testFile = tempDir.resolve("test-image.jpg")

        assertThatThrownBy { service.uploadPhoto(testFile.toString(), true) }
            .isExactlyInstanceOf(FileNotExistsException::class.java)
            .hasMessage("File '$testFile' not exists")
    }

    @Test
    fun `uploadPhoto directory`(@TempDir tempDir: Path) {
        assertThatThrownBy { service.uploadPhoto(tempDir.toString(), true) }
            .isExactlyInstanceOf(FileNotExistsException::class.java)
            .hasMessage("'$tempDir' is are directory")
    }

    @Test
    fun `uploadPhoto unknown file type`(@TempDir tempDir: Path) {
        val testFile = createTempFile(tempDir, "test-image", ".txt")
        assertThatThrownBy { service.uploadPhoto(testFile.toString(), true) }
            .isExactlyInstanceOf(UnsupportedFileTypeException::class.java)
            .hasMessage("Unknown file type for '$testFile'")
    }

    @Test
    fun `uploadPhoto not image file type`() {
        val testFile = createTempFile("test-image", ".txt")

        mediaTypeResolver.stub {
            on { resolve(any()) } doReturn MediaType.TEXT_XML
        }

        assertThatThrownBy { service.uploadPhoto(testFile.toString(), true) }
            .isExactlyInstanceOf(UnsupportedFileTypeException::class.java)
            .hasMessage("Unsupported file type for '$testFile': ${MediaType.TEXT_XML}")
    }
}
