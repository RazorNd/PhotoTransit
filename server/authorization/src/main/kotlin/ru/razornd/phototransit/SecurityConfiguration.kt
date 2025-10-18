package ru.razornd.phototransit

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.boot.autoconfigure.security.StaticResourceLocation.*
import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import org.springframework.security.web.savedrequest.CookieRequestCache

@Configuration
open class SecurityConfiguration {

    @Bean
    @Order(100)
    open fun basicSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                authorize("/error", permitAll)
                authorize(PathRequest.toStaticResources().at(WEB_JARS, CSS, IMAGES, JAVA_SCRIPT), permitAll)
                authorize(EndpointRequest.to("health", "prometheus"), permitAll)
                authorize(anyRequest, authenticated)
            }
            cors { }
            csrf {
                csrfTokenRepository = cookieCsrfRepository()
            }
            formLogin {
                loginPage = "/login"
                permitAll()
            }
            passwordManagement { }
        }
        return http.build()
    }

    @Bean
    open fun cookieRequestCache() = CookieRequestCache()

    @Bean
    open fun cookieCsrfRepository(): CookieCsrfTokenRepository = CookieCsrfTokenRepository()
}
