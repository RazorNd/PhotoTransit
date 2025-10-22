package ru.razornd.phototransit.oauth

import org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.converter.FormHttpMessageConverter
import org.springframework.security.oauth2.client.OAuth2AuthorizationContext
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.OAuth2AuthorizationException
import org.springframework.security.oauth2.core.OAuth2Error
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse
import org.springframework.security.oauth2.core.endpoint.OAuth2DeviceAuthorizationResponse
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter
import org.springframework.security.oauth2.core.http.converter.OAuth2DeviceAuthorizationResponseHttpMessageConverter
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestClient
import org.springframework.web.client.requiredBody
import org.springframework.web.util.UriComponentsBuilder
import java.time.Clock
import java.time.Duration

class DeviceCodeOAuth2AuthorizedClientProvider : OAuth2AuthorizedClientProvider {

    var clock: Clock = Clock.systemUTC()

    var clockSkew: Duration = Duration.ofMinutes(1)

    var poolInterval: Duration = Duration.ofSeconds(15)

    private val restClient = RestClient.builder()
        .messageConverters(
            listOf(
                FormHttpMessageConverter(),
                OAuth2AccessTokenResponseHttpMessageConverter(),
                OAuth2DeviceAuthorizationResponseHttpMessageConverter().apply {
                    supportedMediaTypes = listOf(APPLICATION_JSON)
                }
            )
        )
        .defaultStatusHandler(OAuth2ErrorResponseErrorHandler())
        .build()

    override fun authorize(context: OAuth2AuthorizationContext): OAuth2AuthorizedClient? {
        val clientRegistration = context.clientRegistration.takeIf { it.isDeviceCode } ?: return null

        if (context.authorizedClient?.isExpired() == false) return null

        val deviceAuthResponse = restClient.post()
            .uri {
                UriComponentsBuilder.fromUriString(clientRegistration.providerDetails.issuerUri)
                    .path("/oauth2/device_authorization")
                    .build()
                    .toUri()
            }
            .contentType(APPLICATION_FORM_URLENCODED)
            .body(
                MultiValueMap.fromSingleValue(
                    mapOf(
                        "client_id" to clientRegistration.clientId,
                        "client_secret" to clientRegistration.clientSecret,
                        "scope" to clientRegistration.scopes.joinToString(" ")
                    )
                )
            )
            .retrieve()
            .requiredBody<OAuth2DeviceAuthorizationResponse>()

        println("Open this link in browser and authorize: ${deviceAuthResponse.verificationUriComplete}")

        val accessTokenResponse = pollForAccessToken(clientRegistration, deviceAuthResponse)

        return OAuth2AuthorizedClient(
            clientRegistration,
            context.principal.name,
            accessTokenResponse.accessToken,
            accessTokenResponse.refreshToken
        )
    }

    private fun pollForAccessToken(
        clientRegistration: ClientRegistration,
        deviceAuthResponse: OAuth2DeviceAuthorizationResponse
    ): OAuth2AccessTokenResponse {
        do {
            try {
                return restClient.post()
                    .uri(clientRegistration.providerDetails.tokenUri)
                    .contentType(APPLICATION_FORM_URLENCODED)
                    .body(
                        MultiValueMap.fromSingleValue(
                            mapOf(
                                "client_id" to clientRegistration.clientId,
                                "client_secret" to clientRegistration.clientSecret,
                                "grant_type" to clientRegistration.authorizationGrantType.value,
                                "device_code" to deviceAuthResponse.deviceCode.tokenValue
                            )
                        )
                    )
                    .retrieve()
                    .requiredBody<OAuth2AccessTokenResponse>()
            } catch (authorizationException: OAuth2AuthorizationException) {
                if (!authorizationException.error.isAuthorizationPending()) throw authorizationException
                Thread.sleep(poolInterval)
            }
        } while (true)
    }

    private fun OAuth2Error.isAuthorizationPending() = errorCode == "authorization_pending"

    private val ClientRegistration.isDeviceCode get() = authorizationGrantType == AuthorizationGrantType.DEVICE_CODE

    private fun OAuth2AuthorizedClient.isExpired(): Boolean {
        val expiresAt = accessToken.expiresAt ?: return true
        return clock.instant().isAfter(expiresAt.minus(clockSkew))
    }
}
