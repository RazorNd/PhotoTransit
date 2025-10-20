package ru.razornd.phototransit

import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.config.web.server.invoke
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.security.web.server.savedrequest.NoOpServerRequestCache


@Configuration(proxyBeanMethods = false)
class SecurityConfiguration {

    @Bean
    fun securityFilterChain(http: ServerHttpSecurity) = http {
        securityContextRepository = NoOpServerSecurityContextRepository.getInstance()
        csrf { disable() }
        requestCache {
            requestCache = NoOpServerRequestCache.getInstance()
        }
        authorizeExchange {
            authorize(EndpointRequest.to("health", "prometheus"), permitAll)
            authorize(anyExchange, authenticated)
        }
        oauth2ResourceServer { jwt {} }
    }

}
