package ru.razornd.phototransit

import org.springframework.cloud.gateway.filter.GatewayFilterChain
import org.springframework.cloud.gateway.filter.GlobalFilter
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils
import org.springframework.core.annotation.Order
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Order(0)
@Component
class SubjectUriVariableGlobalFilter : GlobalFilter {

    override fun filter(exchange: ServerWebExchange, chain: GatewayFilterChain): Mono<Void> {
        return exchange.getPrincipal<JwtAuthenticationToken>()
            .doOnNext {
                ServerWebExchangeUtils.putUriTemplateVariables(exchange, mapOf("subject" to it.token.subject))
            }
            .then(chain.filter(exchange))
    }

}
