package ru.razornd.phototransit

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import ru.razornd.phototransit.oauth.DeviceCodeOAuth2AuthorizedClientProvider

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
}
