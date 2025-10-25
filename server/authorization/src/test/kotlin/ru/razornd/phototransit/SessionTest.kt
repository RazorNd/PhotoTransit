package ru.razornd.phototransit

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnectionAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.session.FindByIndexNameSessionRepository
import org.springframework.session.Session
import org.springframework.session.SessionRepository
import java.time.Instant


@Import(PostgresTestcontainersConfiguration::class)
@ImportAutoConfiguration(
    ServiceConnectionAutoConfiguration::class,
    DataSourceAutoConfiguration::class,
    FlywayAutoConfiguration::class,
    TransactionAutoConfiguration::class,
    DataSourceTransactionManagerAutoConfiguration::class,
    JdbcTemplateAutoConfiguration::class,
    SessionAutoConfiguration::class
)
@SpringBootTest(classes = [SessionConfiguration::class])
class SessionTest {

    @Test
    fun <T : Session> `should save session and load from database`(@Autowired sessionRepository: SessionRepository<T>) {
        val session = sessionRepository.createSession()

        val context = SecurityContextHolder.createEmptyContext().apply {
            authentication = UsernamePasswordAuthenticationToken("test-user", "test-password", emptyList())
        }

        session.setAttribute("securityContext", context)

        sessionRepository.save(session)

        val foundSession = sessionRepository.findById(session.id)

        assertThat(foundSession)
            .isNotNull()
            .usingRecursiveComparison()
            .withComparatorForType(Comparator.comparingLong(Instant::toEpochMilli), Instant::class.java)
            .ignoringFields("delegate.sessionAttrs")
            .isEqualTo(session)

        assertThat(foundSession.getAttribute<SecurityContext>("securityContext"))
            .describedAs("security context should be loaded")
            .isNotNull()
            .usingRecursiveComparison()
            .isEqualTo(context)
    }

    @Test
    fun <T : Session> `should insert and then update session`(@Autowired sessionRepository: SessionRepository<T>) {
        val session = sessionRepository.createSession()

        val context = SecurityContextHolder.createEmptyContext().apply {
            authentication = UsernamePasswordAuthenticationToken("test-user", "test-password", emptyList())
        }

        session.setAttribute("attribute-for-update", "old-value")
        session.setAttribute("securityContext", context)

        sessionRepository.save(session)

        session.setAttribute("securityContext", null)
        session.setAttribute("new-attribute", "new-value")
        session.setAttribute("attribute-for-update", "updated-value")

        assertThatCode { sessionRepository.save(session) }.doesNotThrowAnyException()
    }

    @Test
    fun <T : Session> `should delete session`(@Autowired sessionRepository: SessionRepository<T>) {
        val session = sessionRepository.createSession()

        val context = SecurityContextHolder.createEmptyContext().apply {
            authentication = UsernamePasswordAuthenticationToken("test-user", "test-password", emptyList())
        }

        session.setAttribute("securityContext", context)

        sessionRepository.save(session)

        assertThatCode { sessionRepository.deleteById(session.id) }.doesNotThrowAnyException()
        assertThat(sessionRepository.findById(session.id)).isNull()
    }

    @Test
    fun <T : Session> `should find by principal from database`(
        @Autowired sessionRepository: FindByIndexNameSessionRepository<T>
    ) {
        val session = sessionRepository.createSession()

        val context = SecurityContextHolder.createEmptyContext().apply {
            authentication = UsernamePasswordAuthenticationToken("test-user", "test-password", emptyList())
        }

        session.setAttribute("SPRING_SECURITY_CONTEXT", context)

        sessionRepository.save(session)

        val foundSession = sessionRepository.findByPrincipalName("test-user")

        assertThat(foundSession)
            .isNotEmpty()
            .usingRecursiveComparison()
            .withComparatorForType(Comparator.comparingLong(Instant::toEpochMilli), Instant::class.java)
            .ignoringFields("${session.id}.delegate.sessionAttrs")
            .isEqualTo(mapOf(session.id to session))
    }

}
