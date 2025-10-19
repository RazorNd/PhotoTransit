package ru.razornd.phototransit

import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

@Controller
@RequestMapping("/")
class MainController(private val clientRepository: RegisteredClientRepository) {

    @GetMapping("/login")
    fun login() = "login"

    @GetMapping("/login", params = ["error"])
    fun loginError() = ModelAndView("login", mapOf("error" to true))

    @GetMapping("/oauth2/device_verification")
    fun deviceVerification() = "device-verification"

    @GetMapping("/oauth2/device_verification", params = ["error"])
    fun deviceVerificationError() = ModelAndView("device-verification", mapOf("error" to true))

    @GetMapping("/success-authorization")
    fun deviceVerificationSuccess() = ModelAndView("success-athorization")

    @GetMapping("/oauth2/consent", params = ["device_code"])
    fun consent(
        @RequestParam("client_id") clientId: String,
        @RequestParam("scope") scope: String,
        @RequestParam("state") state: String,
        @RequestParam("user_code") userCode: String,
    ) = ModelAndView(
        "consent",
        mapOf(
            "action" to "/oauth2/device_verification",
            "clientId" to clientId,
            "clientName" to clientName(clientId),
            "scopes" to scope.split(" "),
            "state" to state,
            "userCode" to userCode
        )
    )

    private fun clientName(clientId: String) = requireNotNull(clientRepository.findByClientId(clientId)).clientName
}
