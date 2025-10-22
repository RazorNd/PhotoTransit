package ru.razornd.phototransit.http

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest
import org.springframework.core.io.ByteArrayResource
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
import java.time.Instant
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
        "image/jpeg,PROCESSED",
        "image/png,PROCESSED",
        "image/tiff,ORIGINAL",
    )
    fun uploadPhoto(contentType: String, type: String) {
        val photoBytes = "it's a fake photo".toByteArray()
        val createdAt = Instant.parse("2013-09-04T16:58:00Z")

        val mediaType = MediaType.parseMediaType(contentType)
        mockServer.expect(method(HttpMethod.POST))
            .andExpect {
                assertThat(it.uri)
                    .hasPath("/api/origin-store/photos")
                    .hasParameter("name", "test-image")
                    .hasParameter("created_at", createdAt.toString())
                    .hasParameter("type", type)
            }
            .andExpect(content().contentType(mediaType))
            .andExpect(content().bytes(photoBytes))
            .andRespond(withSuccess("""{"id": "a1b21f9f-49a7-48fb-8ad9-5b3896fd0f36"}""", MediaType.APPLICATION_JSON))

        val uploadResponse = client.uploadPhoto(
            ByteArrayResource(photoBytes),
            "test-image",
            createdAt,
            PhotoType.valueOf(type),
            mediaType
        )

        assertThat(uploadResponse)
            .usingRecursiveComparison()
            .isEqualTo(OriginStoreClient.UploadResult(UUID.fromString("a1b21f9f-49a7-48fb-8ad9-5b3896fd0f36")))
    }

}
