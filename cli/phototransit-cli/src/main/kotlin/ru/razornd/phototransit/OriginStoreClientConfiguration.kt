package ru.razornd.phototransit

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor
import org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver
import org.springframework.web.client.RestClient
import org.springframework.web.client.support.RestClientAdapter
import org.springframework.web.service.invoker.HttpServiceProxyFactory
import org.springframework.web.service.invoker.createClient
import ru.razornd.phototransit.http.OriginStoreClient

@EnableConfigurationProperties(PhotoTransitProperties::class)
@Configuration(proxyBeanMethods = false)
class OriginStoreClientConfiguration {

    @Bean
    fun originStoreRestClient(
        properties: PhotoTransitProperties,
        builder: RestClient.Builder,
        clientManager: OAuth2AuthorizedClientManager
    ): RestClient = builder.baseUrl(properties.baseUrl)
        .requestInterceptor(OAuth2ClientHttpRequestInterceptor(clientManager))
        .requestInitializer {
            RequestAttributeClientRegistrationIdResolver.clientRegistrationId("phototransit-cli").accept(it.attributes)
        }
        .build()

    @Bean
    fun originStoreClient(originStoreRestClient: RestClient): OriginStoreClient {
        val clientAdapter = RestClientAdapter.create(originStoreRestClient)

        val proxyFactory = HttpServiceProxyFactory.builderFor(clientAdapter).build()

        return proxyFactory.createClient()
    }

}
