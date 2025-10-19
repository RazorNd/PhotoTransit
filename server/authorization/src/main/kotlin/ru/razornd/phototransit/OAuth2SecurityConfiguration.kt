package ru.razornd.phototransit

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer.authorizationServer
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher

@Configuration(proxyBeanMethods = false)
class OAuth2SecurityConfiguration {
    @Bean
    @Order(0)
    fun authorizationServerSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests { authorize(anyRequest, authenticated) }
            cors { }
            with(authorizationServer()) {
                securityMatcher(endpointsMatcher)
                deviceVerificationEndpoint {
                    it.errorResponseHandler(SimpleUrlAuthenticationFailureHandler("/oauth2/device_verification?error"))
                    it.deviceVerificationResponseHandler(SimpleUrlAuthenticationSuccessHandler("/success-authorization"))
                    it.consentPage("/oauth2/consent?device_code")
                }
            }
            oauth2ResourceServer {
                jwt { }
            }
            exceptionHandling {
                defaultAuthenticationEntryPointFor(
                    LoginUrlAuthenticationEntryPoint("/login"),
                    authenticationEntryPointRequestMatcher()
                )
            }
        }

        return http.build()
    }

    private fun authenticationEntryPointRequestMatcher() = MediaTypeRequestMatcher(MediaType.TEXT_HTML).apply {
        setIgnoredMediaTypes(setOf(MediaType.ALL))
    }
}
