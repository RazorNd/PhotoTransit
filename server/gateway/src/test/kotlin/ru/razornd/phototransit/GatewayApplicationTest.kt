package ru.razornd.phototransit

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.factories.DefaultJWSSignerFactory
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.wiremock.spring.EnableWireMock

@EnableWireMock
@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    properties = [
        "oauth2.identity-provider.url=\${wiremock.server.baseUrl}",
        "routes.origin-store=\${wiremock.server.baseUrl}"
    ]
)
class GatewayApplicationTest {

    @Autowired
    lateinit var client: WebTestClient

    @Value("\${wiremock.server.baseUrl}")
    lateinit var wiremockBaseUrl: String

    @BeforeEach
    fun setUp() {
        stubFor(
            get("/.well-known/openid-configuration")
                .willReturn(
                    okJson(
                        """
                        {
                            "issuer": "$wiremockBaseUrl",
                            "jwks_uri": "$wiremockBaseUrl/oauth2/jwks"
                        }
                        """.trimIndent()
                    )
                )
        )

        stubFor(get("/oauth2/jwks").willReturn(okJson(JWKSet(rsaKey).toString(true))))
    }

    @Test
    fun originStoreRoute() {
        val responseJson = """{"id": "1d0886a7-e675-43cb-acb9-befda652e530"}"""

        stubFor(post(urlPathEqualTo("/api/origin-store/photos")).willReturn(okJson(responseJson)))

        client.post()
            .uri("/api/origin-store/photos")
            .headers { it.setBearerAuth(createToken("origin-store:photo:upload")) }
            .bodyValue("it's a fake photo")
            .exchange()
            .expectStatus().isOk
            .expectBody<String>()
            .isEqualTo(responseJson)

        verify(
            postRequestedFor(urlPathEqualTo("/api/origin-store/photos")).withQueryParam(
                "owner",
                equalTo("test-user")
            )
        )
    }

    private fun createToken(scope: String): String = SignedJWT(
        jwsHeader,
        JWTClaimsSet.Builder()
            .issuer(wiremockBaseUrl)
            .subject("test-user")
            .claim("scope", scope)
            .build()
    ).apply { sign(signer) }.serialize()

    companion object {
        private val rsaKey = RSAKeyGenerator(2048)
            .algorithm(JWSAlgorithm.RS256)
            .keyID("test-key-id")
            .generate()

        private val jwsHeader = JWSHeader.Builder(JWSAlgorithm.RS256)
            .keyID(rsaKey.keyID)
            .build()

        private val signer = DefaultJWSSignerFactory().createJWSSigner(rsaKey)
    }
}
