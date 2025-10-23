package ru.razornd.phototransit.oauth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.json.JsonTest
import org.springframework.boot.test.json.BasicJsonTester
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.inputStream
import kotlin.io.path.readText
import kotlin.io.path.writeText

private const val testUser = "test-user"

private val clientRegistration = ClientRegistration.withRegistrationId("test-client")
    .authorizationGrantType(AuthorizationGrantType.DEVICE_CODE)
    .build()


private val authorizedClient = OAuth2AuthorizedClient(
    clientRegistration,
    testUser,
    OAuth2AccessToken(
        OAuth2AccessToken.TokenType.BEARER,
        "access-token",
        Instant.parse("1986-08-05T06:41:49Z"),
        Instant.parse("1986-08-05T06:51:49Z"),
        setOf("scope1", "scope2")
    ),
    OAuth2RefreshToken(
        "refresh-token",
        Instant.parse("1986-08-05T06:41:49Z"),
        Instant.parse("1986-08-05T18:41:49Z")
    )
)

private const val serializedAuthentication = """
{
      "accessToken": {
        "@class": "org.springframework.security.oauth2.core.OAuth2AccessToken",
        "tokenValue": "access-token",
        "issuedAt": "1986-08-05T06:41:49Z",
        "expiresAt": "1986-08-05T06:51:49Z",
        "tokenType": {
          "value": "Bearer"
        },
        "scopes": [ "java.util.Collections${'$'}UnmodifiableSet", ["scope1", "scope2"]]
      },
      "refreshToken": {
        "@class": "org.springframework.security.oauth2.core.OAuth2RefreshToken",
        "tokenValue": "refresh-token",
        "issuedAt": "1986-08-05T06:41:49Z",
        "expiresAt": "1986-08-05T18:41:49Z"
      }
}
"""

@JsonTest
class FileBaseOAuth2AuthorizedClientServiceTest {

    @Autowired
    lateinit var jsonTester: BasicJsonTester

    @Test
    fun loadAuthorizedClient(@TempDir tempDir: Path) {
        val tokenFilePath = tempDir.resolve("authorized-client.json").apply {
            writeText(serializedAuthentication)
        }

        val clientService = FileBaseOAuth2AuthorizedClientService(tokenFilePath, clientRegistration, testUser)

        val client =
            clientService.loadAuthorizedClient<OAuth2AuthorizedClient>(clientRegistration.registrationId, testUser)

        assertThat(client)
            .usingRecursiveComparison()
            .isEqualTo(authorizedClient)
    }

    @Test
    fun `loadAuthorizedClient file not exists`(@TempDir tempDir: Path) {
        val tokenFilePath = tempDir.resolve("not-exists.json")

        val clientService = FileBaseOAuth2AuthorizedClientService(tokenFilePath, clientRegistration, testUser)

        val client =
            clientService.loadAuthorizedClient<OAuth2AuthorizedClient>(clientRegistration.registrationId, testUser)

        assertThat(client).isNull()
    }

    @Test
    fun `loadAuthorizedClient wrong client`(@TempDir tempDir: Path) {
        val tokenFilePath = tempDir.resolve("authorization.json").apply {
            writeText(serializedAuthentication)
        }

        val clientService = FileBaseOAuth2AuthorizedClientService(tokenFilePath, clientRegistration, testUser)

        val client =
            clientService.loadAuthorizedClient<OAuth2AuthorizedClient>("not-supported-client", testUser)

        assertThat(client).isNull()
    }

    @Test
    fun `loadAuthorizedClient wrong user`(@TempDir tempDir: Path) {
        val tokenFilePath = tempDir.resolve("authorization.json").apply {
            writeText(serializedAuthentication)
        }

        val clientService = FileBaseOAuth2AuthorizedClientService(tokenFilePath, clientRegistration, testUser)

        val client =
            clientService.loadAuthorizedClient<OAuth2AuthorizedClient>(clientRegistration.registrationId, "wrong-user")

        assertThat(client).isNull()
    }

    @Test
    fun saveAuthorizedClient(@TempDir tempDir: Path) {
        val tokenFilePath = tempDir.resolve("save.json")

        val clientService = FileBaseOAuth2AuthorizedClientService(tokenFilePath, clientRegistration, testUser)

        clientService.saveAuthorizedClient(authorizedClient, TestingAuthenticationToken(testUser, "test-password"))

        assertThat(jsonTester.from(tokenFilePath.inputStream()))
            .describedAs("Saved file content\n" + tokenFilePath.readText())
            .isStrictlyEqualToJson(serializedAuthentication)
    }

    @Test
    fun `saveAuthorizedClient override existing file`(@TempDir tempDir: Path) {
        val tokenFilePath = tempDir.resolve("existing.json").apply {
            writeText("""{"some": "data"}""")
        }

        val clientService = FileBaseOAuth2AuthorizedClientService(tokenFilePath, clientRegistration, testUser)

        clientService.saveAuthorizedClient(authorizedClient, TestingAuthenticationToken(testUser, "test-password"))

        assertThat(jsonTester.from(tokenFilePath.inputStream()))
            .describedAs("Saved file content\n" + tokenFilePath.readText())
            .isStrictlyEqualToJson(serializedAuthentication)
    }

    @Test
    fun `saveAuthorizedClient wrong client`(@TempDir tempDir: Path) {
        val tokenFilePath = tempDir.resolve("save wrong.json")

        val clientService = FileBaseOAuth2AuthorizedClientService(tokenFilePath, clientRegistration, testUser)

        val authorizedClient = OAuth2AuthorizedClient(
            ClientRegistration.withRegistrationId("wrong-client")
                .authorizationGrantType(AuthorizationGrantType.DEVICE_CODE)
                .build(),
            testUser,
            OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token",
                Instant.parse("1994-08-31T03:06:39Z"),
                Instant.parse("1994-08-31T03:16:39Z"),
            )
        )

        clientService.saveAuthorizedClient(authorizedClient, TestingAuthenticationToken(testUser, "test-password"))

        assertThat(tokenFilePath).doesNotExist()
    }

    @Test
    fun `saveAuthorizedClient wrong user`(@TempDir tempDir: Path) {
        val tokenFilePath = tempDir.resolve("save wrong.json")

        val clientService = FileBaseOAuth2AuthorizedClientService(tokenFilePath, clientRegistration, testUser)

        clientService.saveAuthorizedClient(authorizedClient, TestingAuthenticationToken("wrong-user", "test-password"))

        assertThat(tokenFilePath).doesNotExist()
    }

    @Test
    fun removeAuthorizedClient(@TempDir tempDir: Path) {
        val tokenFilePath = tempDir.resolve("remove.json").apply {
            writeText(serializedAuthentication)
        }

        val service = FileBaseOAuth2AuthorizedClientService(tokenFilePath, clientRegistration, testUser)

        service.removeAuthorizedClient(clientRegistration.registrationId, testUser)

        assertThat(tokenFilePath).doesNotExist()
    }

    @Test
    fun `removeAuthorizedClient wrong client`(@TempDir tempDir: Path) {
        val tokenFilePath = tempDir.resolve("do note remove.json").apply {
            writeText(serializedAuthentication)
        }

        val service = FileBaseOAuth2AuthorizedClientService(tokenFilePath, clientRegistration, testUser)

        service.removeAuthorizedClient("wrong-client-id", testUser)

        assertThat(tokenFilePath).exists()
    }

    @Test
    fun `removeAuthorizedClient wrong user`(@TempDir tempDir: Path) {
        val tokenFilePath = tempDir.resolve("do note remove.json").apply {
            writeText(serializedAuthentication)
        }

        val service = FileBaseOAuth2AuthorizedClientService(tokenFilePath, clientRegistration, testUser)

        service.removeAuthorizedClient(clientRegistration.registrationId, "wrong-user")

        assertThat(tokenFilePath).exists()
    }
}
