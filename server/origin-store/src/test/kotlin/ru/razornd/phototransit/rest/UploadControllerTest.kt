package ru.razornd.phototransit.rest

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.reactive.server.WebTestClient
import ru.razornd.phototransit.PhotoId
import ru.razornd.phototransit.UserId
import ru.razornd.phototransit.service.ImageFormat
import ru.razornd.phototransit.service.PhotoType
import ru.razornd.phototransit.service.UploadPhoto
import ru.razornd.phototransit.service.UploadService
import java.io.ByteArrayInputStream
import java.time.Instant
import kotlin.random.Random

private val IMAGE_TIFF = MediaType.parseMediaType("image/tiff")

@WebMvcTest(controllers = [UploadController::class])
class UploadControllerTest {

    @Autowired
    lateinit var testClient: WebTestClient

    @MockitoBean
    lateinit var service: UploadService

    @ParameterizedTest
    @EnumSource(ImageFormat::class)
    fun upload(imageFormat: ImageFormat) {
        val photoId = PhotoId("7197432e-7ace-4601-83e0-7267a3dd8824")
        val bytes = Random.nextBytes(512)
        val uploadPhoto = UploadPhoto(
            "test",
            ByteArrayInputStream(bytes),
            Instant.parse("2013-09-04T16:58:00Z"),
            UserId("c9997372-ef1a-4b9a-8478-873c2ac7ed70"),
            PhotoType.ORIGINAL
        )
        val dtoCaptor = argumentCaptor<UploadPhoto>()

        service.stub {
            on { savePhoto(any(), any()) } doReturn photoId
        }

        testClient.post()
            .uri { builder ->
                with(builder) {
                    path("/api/origin-store/photos")
                    queryParam("name", uploadPhoto.name)
                    queryParam("created_at", uploadPhoto.createdAt.toString())
                    queryParam("owner", uploadPhoto.owner.id.toString())
                    queryParam("type", uploadPhoto.type.name)
                    build()
                }
            }
            .contentType(imageFormat.toMimeType())
            .bodyValue(bytes)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.id").isEqualTo(photoId.toString())

        verify(service).savePhoto(dtoCaptor.capture(), eq(imageFormat))

        assertThat(dtoCaptor.firstValue)
            .usingRecursiveComparison()
            .ignoringFields("inputStream")
            .isEqualTo(uploadPhoto)

        assertThat(dtoCaptor.singleValue.inputStream)
            .describedAs("Upload image")
            .hasBinaryContent(bytes)
    }

    private fun ImageFormat.toMimeType(): MediaType = when (this) {
        ImageFormat.JPEG -> MediaType.IMAGE_JPEG
        ImageFormat.PNG -> MediaType.IMAGE_PNG
        ImageFormat.TIFF -> IMAGE_TIFF
    }
}
