package ru.razornd.phototransit

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/")
class MainController {

    @GetMapping("/login")
    fun login() = "login"

    @GetMapping("/login", params = ["error"])
    fun loginError() = ModelAndView("login", mapOf("error" to true))

}
