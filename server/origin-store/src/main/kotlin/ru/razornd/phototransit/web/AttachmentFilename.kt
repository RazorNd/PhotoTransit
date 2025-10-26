package ru.razornd.phototransit.web

import org.springframework.core.MethodParameter
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer


@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class AttachmentFilename

internal class AttachmentFilenameArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(AttachmentFilename::class.java) &&
                parameter.nestedIfOptional().nestedParameterType == String::class.java
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): String {
        val contentDisposition = parseContentDisposition(webRequest)

        return contentDisposition.filename ?: throw MissingRequestHeaderException(
            HttpHeaders.CONTENT_DISPOSITION,
            parameter
        )
    }

    private fun parseContentDisposition(webRequest: NativeWebRequest): ContentDisposition {
        val header = webRequest.getHeader(HttpHeaders.CONTENT_DISPOSITION)
        if (header != null) {
            return ContentDisposition.parse(header)
        }
        return ContentDisposition.empty()
    }
}
