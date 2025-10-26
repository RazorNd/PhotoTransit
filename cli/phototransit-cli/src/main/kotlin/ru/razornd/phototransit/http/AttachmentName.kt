package ru.razornd.phototransit.http

import org.springframework.core.MethodParameter
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.web.service.invoker.HttpRequestValues
import org.springframework.web.service.invoker.HttpServiceArgumentResolver

@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class AttachmentName

internal class AttachmentNameArgumentResolver : HttpServiceArgumentResolver {
    override fun resolve(
        argument: Any?,
        parameter: MethodParameter,
        requestValues: HttpRequestValues.Builder
    ): Boolean {
        if (!parameter.hasParameterAnnotation(AttachmentName::class.java)) return false
        if (argument == null) {
            require(parameter.isOptional) { "Attachment name is required" }
            return true
        }

        val contentDisposition = ContentDisposition.attachment()
            .filename(argument.toString(), Charsets.UTF_8)
            .build()

        requestValues.addHeader(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())

        return true
    }

}
