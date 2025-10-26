package ru.razornd.phototransit.http

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.core.io.ByteArrayResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.client.MockRestServiceServer
import org.springframework.test.web.client.match.MockRestRequestMatchers.content
import org.springframework.test.web.client.match.MockRestRequestMatchers.method
import org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess
import ru.razornd.phototransit.OriginStoreClientConfiguration
import ru.razornd.phototransit.http.OriginStoreClient.PhotoType
import java.util.*

@MockitoBean(types = [OAuth2AuthorizedClientManager::class])
@RestClientTest(OriginStoreClientConfiguration::class)
class OriginStoreClientTest {

    @Autowired
    lateinit var client: OriginStoreClient

    @Autowired
    lateinit var mockServer: MockRestServiceServer

    @ParameterizedTest
    @CsvSource(
        "image/jpeg,MASTER,test.jpeg",
        "image/png,MASTER,image.png",
        "image/tiff,ORIGINAL,master.tiff",
    )
    fun uploadPhoto(contentType: String, type: String, filename: String) {
        val photoBytes = "it's a fake photo".toByteArray()

        val mediaType = MediaType.parseMediaType(contentType)
        mockServer.expect(method(HttpMethod.POST))
            .andExpect {
                assertThat(it.uri)
                    .hasPath("/api/origin-store/photos")
                    .hasParameter("type", type)
            }
            .andExpect { assertContentDisposition(it.headers, filename) }
            .andExpect(content().contentType(mediaType))
            .andExpect(content().bytes(photoBytes))
            .andRespond(withSuccess("""{"id": "a1b21f9f-49a7-48fb-8ad9-5b3896fd0f36"}""", MediaType.APPLICATION_JSON))

        val uploadResponse = client.uploadPhoto(
            ByteArrayResource(photoBytes),
            filename,
            PhotoType.valueOf(type),
            mediaType
        )

        assertThat(uploadResponse)
            .usingRecursiveComparison()
            .isEqualTo(OriginStoreClient.UploadResult(UUID.fromString("a1b21f9f-49a7-48fb-8ad9-5b3896fd0f36")))
    }

    private fun assertContentDisposition(httpHeaders: HttpHeaders, filename: String) {
        val contentDispositionValue = httpHeaders.getFirst(HttpHeaders.CONTENT_DISPOSITION)

        assertThat(contentDispositionValue)
            .describedAs("Content-Disposition header value")
            .isNotNull()

        val contentDisposition = ContentDisposition.parse(checkNotNull(contentDispositionValue))

        assertThat(contentDisposition.type)
            .describedAs("Content-Disposition: Type")
            .isEqualTo("attachment")

        assertThat(contentDisposition.filename)
            .describedAs("Content-Disposition: Filename")
            .isEqualTo(filename)
    }

}
