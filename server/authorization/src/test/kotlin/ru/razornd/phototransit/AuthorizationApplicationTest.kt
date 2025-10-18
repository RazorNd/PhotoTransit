package ru.razornd.phototransit

import com.microsoft.playwright.Page
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.context.annotation.Import

@SpringBootTest(
    webEnvironment = RANDOM_PORT,
    properties = [
        "spring.security.user.name=user",
        "spring.security.user.password=password",
    ]
)
@Import(PostgresTestcontainersConfiguration::class, PlaywrightConfiguration::class)
class AuthorizationApplicationTest {

    @Test
    fun login(@Autowired page: Page) {
        page.navigate("/")
        page.waitForURL("/login")

        page.fill("input[name=username]", "user")
        page.fill("input[name=password]", "password")
        page.click("button[type=submit]")

        page.waitForURL("/")
    }
}
