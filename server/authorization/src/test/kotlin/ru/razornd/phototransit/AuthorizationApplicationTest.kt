package ru.razornd.phototransit

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.microsoft.playwright.Page
import com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.returnResult
import org.springframework.util.MultiValueMap

@AutoConfigureWebTestClient
@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    properties = [
        "spring.security.user.name=user",
        "spring.security.user.password=password",
        "logging.level.org.springframework.security=trace"
    ]
)
@Import(PostgresTestcontainersConfiguration::class, PlaywrightConfiguration::class)
class AuthorizationApplicationTest {

    @Autowired
    lateinit var client: WebTestClient

    @Test
    fun cliApplicationDeviceCodeGrant(@Autowired page: Page) {
        val scope = "origin-store:photo:upload"
        val deviceAuthorization = client.post()
            .uri("/oauth2/device_authorization")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(
                MultiValueMap.fromSingleValue(
                    mapOf(
                        "client_id" to "phototransit-cli",
                        "client_secret" to "changeme",
                        "scope" to scope
                    )
                )
            )
            .exchange()
            .expectStatus().isOk()
            .returnResult<DeviceAuthorization>()
            .responseBody
            .single()
            .block() ?: error("Empty response")

        page.navigate(deviceAuthorization.verificationUri)

        page.fill("input[name=username]", "user")
        page.fill("input[name=password]", "password")
        page.click("button[type=submit]")

        assertThat(page.getByText("Enter Your Device Code")).isVisible()

        page.fill("input[name=user_code]", deviceAuthorization.userCode)
        page.click("button[type=submit]")

        assertThat(page.getByText("Consent required")).isVisible()

        page.click("button[value=approve]")

        assertThat(page.getByText("Authorization successful")).isVisible()

        client.post()
            .uri("/oauth2/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .bodyValue(
                MultiValueMap.fromSingleValue(
                    mapOf(
                        "client_id" to "phototransit-cli",
                        "client_secret" to "changeme",
                        "grant_type" to "urn:ietf:params:oauth:grant-type:device_code",
                        "device_code" to deviceAuthorization.deviceCode
                    )
                )
            )
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.access_token").isNotEmpty()
            .jsonPath("$.refresh_token").isNotEmpty()
            .jsonPath("$.scope").isEqualTo(scope)
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    data class DeviceAuthorization(
        val deviceCode: String,
        val userCode: String,
        val verificationUri: String,
        val verificationUriComplete: String
    )
}
