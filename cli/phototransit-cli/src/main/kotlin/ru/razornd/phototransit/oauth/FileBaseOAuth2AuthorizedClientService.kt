package ru.razornd.phototransit.oauth

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.annotation.RegisterReflection
import org.springframework.security.core.Authentication
import org.springframework.security.jackson2.CoreJackson2Module
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.jackson2.OAuth2ClientJackson2Module
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.OAuth2AccessToken
import org.springframework.security.oauth2.core.OAuth2RefreshToken
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.nio.file.Path
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.deleteIfExists
import kotlin.io.path.exists
import kotlin.io.path.inputStream

private val logger = LoggerFactory.getLogger(FileBaseOAuth2AuthorizedClientService::class.qualifiedName)

class FileBaseOAuth2AuthorizedClientService(
    private val tokenFilePath: Path,
    private val clientRegistration: ClientRegistration,
    private val principalName: String
) : OAuth2AuthorizedClientService {

    private val objectMapper = ObjectMapper().apply {
        registerModules(
            KotlinModule.Builder().build(),
            JavaTimeModule(),
            CoreJackson2Module(),
            OAuth2ClientJackson2Module()
        )
        configure(SerializationFeature.CLOSE_CLOSEABLE, true)
        configure(SerializationFeature.INDENT_OUTPUT, true)
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
    }

    override fun <T : OAuth2AuthorizedClient> loadAuthorizedClient(
        clientRegistrationId: String,
        principalName: String
    ): T? {
        if (!isValidClientSession(clientRegistrationId, principalName)) return null
        if (!tokenFilePath.exists()) return null
        val (accessToken, refreshToken) = objectMapper.readValue<Authorization>(
            tokenFilePath.inputStream(StandardOpenOption.READ)
        )
        @Suppress("UNCHECKED_CAST")
        return OAuth2AuthorizedClient(clientRegistration, principalName, accessToken, refreshToken) as T
    }

    override fun saveAuthorizedClient(authorizedClient: OAuth2AuthorizedClient, principal: Authentication) {
        if (isValidClientSession(authorizedClient.clientRegistration.registrationId, principal.name)) {
            val channel = FileChannel.open(
                tokenFilePath,
                setOf(
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING
                ),
                PosixFilePermissions.asFileAttribute(
                    setOf(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE
                    )
                )
            )

            objectMapper.writeValue(
                Channels.newOutputStream(channel),
                Authorization(
                    accessToken = authorizedClient.accessToken,
                    refreshToken = authorizedClient.refreshToken
                )
            )
        }
    }

    override fun removeAuthorizedClient(clientRegistrationId: String, principalName: String) {
        if (isValidClientSession(clientRegistrationId, principalName)) {
            tokenFilePath.deleteIfExists()
        }
    }

    private fun isValidClientSession(clientRegistrationId: String, principalName: String): Boolean {
        if (clientRegistration.registrationId != clientRegistrationId) {
            logger.warn("Invalid client registration id: {}", clientRegistrationId)
            return false
        }
        if (principalName != this.principalName) {
            logger.warn("Invalid principal name: {}", principalName)
            return false
        }
        return true
    }

    @RegisterReflection(
        memberCategories = [
            MemberCategory.INTROSPECT_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS
        ]
    )
    private data class Authorization(val accessToken: OAuth2AccessToken, val refreshToken: OAuth2RefreshToken?)
}
