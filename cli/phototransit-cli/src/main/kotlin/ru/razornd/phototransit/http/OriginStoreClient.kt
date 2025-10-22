package ru.razornd.phototransit.http

import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.annotation.RegisterReflection
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.service.annotation.HttpExchange
import org.springframework.web.service.annotation.PostExchange
import java.time.Instant
import java.util.*

@RegisterReflection(memberCategories = [MemberCategory.INTROSPECT_PUBLIC_METHODS])
@HttpExchange("/api/origin-store/")
interface OriginStoreClient {

    @RegisterReflection(
        classes = [MediaType::class],
        memberCategories = [
            MemberCategory.INTROSPECT_PUBLIC_METHODS,
            MemberCategory.INTROSPECT_PUBLIC_CONSTRUCTORS
        ]
    )
    @PostExchange("photos")
    fun uploadPhoto(
        @RequestBody photo: Resource,
        @RequestParam name: String,
        @RequestParam("created_at") createdAt: Instant,
        @RequestParam type: PhotoType,
        @RequestHeader("Content-Type") contentType: MediaType
    ): UploadResult

    enum class PhotoType {
        ORIGINAL, PROCESSED
    }

    @RegisterReflection(
        memberCategories = [
            MemberCategory.INTROSPECT_PUBLIC_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS
        ]
    )
    data class UploadResult(val id: UUID)

}
