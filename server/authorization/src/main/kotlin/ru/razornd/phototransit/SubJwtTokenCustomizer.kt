package ru.razornd.phototransit

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.oidc.StandardClaimNames
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer
import org.springframework.stereotype.Component

@Component
class SubJwtTokenCustomizer : OAuth2TokenCustomizer<JwtEncodingContext> {

    private val adminSubject = "58febaab-4e5f-448a-98db-e2cd8774205e"

    override fun customize(context: JwtEncodingContext) {
        val authentication = context.getPrincipal<Authentication>()

        authentication.authorities.find { it.authority == "ROLE_admin" } ?: return

        context.claims.subject(adminSubject)
        context.claims.claims { it[StandardClaimNames.NAME] = authentication.name }
    }

}
