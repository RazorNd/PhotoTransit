package ru.razornd.phototransit

import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class CsrfControllerAdvice {
    @ModelAttribute("csrfToken")
    fun csrf(token: CsrfToken?) = token
}
