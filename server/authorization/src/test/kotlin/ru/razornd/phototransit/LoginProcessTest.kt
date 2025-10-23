package ru.razornd.phototransit

import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.stub
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.web.savedrequest.CookieRequestCache
import org.springframework.security.web.savedrequest.SimpleSavedRequest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post

@MockitoBean(types = [RegisteredClientRepository::class])
@WebMvcTest(MainController::class)
@Import(SecurityConfiguration::class)
class LoginProcessTest {

    @Autowired
    lateinit var mvc: MockMvc

    @MockitoBean
    lateinit var userDetailsService: UserDetailsService

    @MockitoBean
    lateinit var requestCache: CookieRequestCache

    @Test
    fun successLogin() {
        userDetailsService.stub {
            on { loadUserByUsername("user") } doReturn User("user", "{noop}<PASSWORD>", emptyList())
        }

        requestCache.stub {
            on { getRequest(any(), any()) } doReturn SimpleSavedRequest("/oauth2/authorization")
        }


        mvc.post("/login") {
            with(csrf())
            formField("username", "user")
            formField("password", "<PASSWORD>")
        }.andExpect {
            status { is3xxRedirection() }
            header { string("location", "/oauth2/authorization") }
        }
    }

    @Test
    fun userNotFound() {
        userDetailsService.stub {
            on { loadUserByUsername("user") } doThrow UsernameNotFoundException("User not found")
        }

        mvc.post("/login") {
            with(csrf())
            formField("username", "user")
            formField("password", "<PASSWORD>")
        }.andExpect {
            status { is3xxRedirection() }
            header { string("location", "/login?error") }
        }
    }

    @Test
    fun wrongPassword() {
        userDetailsService.stub {
            on { loadUserByUsername("user") } doReturn User("user", "{noop}wrong", emptyList())
        }

        mvc.post("/login") {
            with(csrf())
            formField("username", "user")
            formField("password", "<PASSWORD>")
        }.andExpect {
            status { is3xxRedirection() }
            header { string("location", "/login?error") }
        }
    }
}
