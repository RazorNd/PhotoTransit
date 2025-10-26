package ru.razornd.phototransit.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders.CONTENT_DISPOSITION
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import ru.razornd.phototransit.PhotoId
import ru.razornd.phototransit.UserId
import ru.razornd.phototransit.service.PhotoType
import ru.razornd.phototransit.service.UploadPhoto
import ru.razornd.phototransit.service.UploadService
import ru.razornd.phototransit.web.AttachmentFilenameArgumentResolver
import java.io.ByteArrayInputStream

private val IMAGE_TIFF = MediaType.parseMediaType("image/tiff")

@Import(AttachmentFilenameArgumentResolver::class)
@WebMvcTest(controllers = [UploadController::class])
class UploadControllerTest {

    @Autowired
    lateinit var testClient: WebTestClient

    @MockitoBean
    lateinit var service: UploadService

    private val bytes = "[It's a test image content]".toByteArray()

    private val userId = "c9997372-ef1a-4b9a-8478-873c2ac7ed70"

    @ParameterizedTest
    @CsvSource(
        "image/tiff,.tiff,MASTER",
        "image/jpeg,.jpg,MASTER",
        "image/tiff,.tiff,ORIGINAL",
        "image/jpeg,.jpg,ORIGINAL",
        "image/png,.png,ORIGINAL",
        "image/x-sony-arw,.arw,ORIGINAL",
        "image/x-pict,.pic,ORIGINAL",
        "image/heic,.heic,ORIGINAL",
    )
    fun upload(contentType: String, extension: String, type: PhotoType) {
        val photoId = PhotoId("7197432e-7ace-4601-83e0-7267a3dd8824")
        val uploadPhoto = UploadPhoto(
            "test$extension",
            ByteArrayInputStream(bytes),
            UserId(userId),
            type
        )
        val dtoCaptor = argumentCaptor<UploadPhoto>()

        service.stub {
            on { savePhoto(any()) } doReturn photoId
        }

        makeUploadRequest(uploadPhoto.type, uploadPhoto.filename, MediaType.parseMediaType(contentType))
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isEqualTo(photoId.toString())

        verify(service).savePhoto(dtoCaptor.capture())

        assertThat(dtoCaptor.singleValue)
            .usingRecursiveComparison()
            .ignoringFields("inputStream")
            .isEqualTo(uploadPhoto)

        assertThat(dtoCaptor.singleValue.inputStream)
            .describedAs("Upload image")
            .hasBinaryContent(bytes)
    }

    @ParameterizedTest
    @CsvSource(
        "image/png,.png,MASTER",
        "image/x-sony-arw,.arw,MASTER",
        "image/x-pict,.pic,MASTER",
        "image/heic,.heic,MASTER",
    )
    fun `upload not supported file type for master`(contentType: String, extension: String, type: PhotoType) {
        makeUploadRequest(type, "test$extension", MediaType.parseMediaType(contentType))
            .exchange()
            .expectStatus().isBadRequest
    }

    @CsvSource(
        "application/octet-stream,.dat,ORIGINAL",
        "application/octet-stream,.dat,MASTER",
        "text/plain,.txt,ORIGINAL",
        "text/plain,.txt,MASTER",
    )
    @ParameterizedTest
    fun `upload not image`(contentType: String, extension: String, type: PhotoType) {
        makeUploadRequest(type, "test$extension", MediaType.parseMediaType(contentType))
            .exchange()
            .expectStatus().isEqualTo(415)
    }

    @Test
    fun `upload with missing attachment filename`() {
        makeUploadRequest(PhotoType.MASTER, "test.jpg", IMAGE_TIFF)
            .headers {
                it.remove(CONTENT_DISPOSITION)
            }
            .exchange()
            .expectStatus().isBadRequest()
    }

    @Test
    fun `upload with default type`() {
        service.stub {
            on { savePhoto(any()) } doReturn PhotoId("7197432e-7ace-4601-83e0-7267a3dd8824")
        }

        testClient.post()
            .uri { builder ->
                with(builder) {
                    path("/api/origin-store/photos")
                    queryParam("owner", userId)
                    build()
                }
            }
            .headers {
                it.contentDisposition = ContentDisposition.attachment()
                    .filename("test.jpg", Charsets.UTF_8)
                    .build()
            }
            .contentType(MediaType.IMAGE_JPEG)
            .bodyValue(bytes)
            .exchange()
            .expectStatus().isCreated
    }

    private fun makeUploadRequest(photoType: PhotoType, filename: String, contentType: MediaType) =
        testClient.post()
            .uri { builder ->
                with(builder) {
                    path("/api/origin-store/photos")
                    queryParam("owner", userId)
                    queryParam("type", photoType.name)
                    build()
                }
            }
            .headers {
                it.contentDisposition = ContentDisposition.attachment()
                    .filename(filename, Charsets.UTF_8)
                    .build()
            }
            .contentType(contentType)
            .bodyValue(bytes)

}
