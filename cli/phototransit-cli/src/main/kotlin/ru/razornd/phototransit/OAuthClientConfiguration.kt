package ru.razornd.phototransit

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import ru.razornd.phototransit.oauth.DeviceCodeOAuth2AuthorizedClientProvider
import ru.razornd.phototransit.oauth.FileBaseOAuth2AuthorizedClientService
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.createDirectory
import kotlin.io.path.notExists

@Configuration(proxyBeanMethods = false)
class OAuthClientConfiguration {

    @Bean
    fun oAuth2AuthorizedClientManager(
        clientsRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService
    ): OAuth2AuthorizedClientManager {
        return AuthorizedClientServiceOAuth2AuthorizedClientManager(clientsRepository, authorizedClientService).apply {
            setAuthorizedClientProvider(DeviceCodeOAuth2AuthorizedClientProvider())
        }
    }

    @Bean
    fun fileBaseAuthorizedClientService(
        clientsRepository: ClientRegistrationRepository
    ): FileBaseOAuth2AuthorizedClientService {
        val storageDirectory = Path.of(System.getProperty("user.home"), ".photo-transit")

        if (storageDirectory.notExists()) {
            storageDirectory.createDirectory(
                PosixFilePermissions.asFileAttribute(
                    setOf(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE,
                        PosixFilePermission.OWNER_EXECUTE
                    )
                )
            )
        }


        return FileBaseOAuth2AuthorizedClientService(
            storageDirectory.resolve("authorization.json"),
            clientsRepository.findByRegistrationId("phototransit-cli"),
            System.getProperty("user.name")
        )
    }
}
