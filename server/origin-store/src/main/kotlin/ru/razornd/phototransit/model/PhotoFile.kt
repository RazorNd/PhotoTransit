package ru.razornd.phototransit.model

import ru.razornd.phototransit.PhotoId
import ru.razornd.phototransit.service.PhotoType
import java.util.*

data class PhotoFile(
    val id: UUID,
    val photoId: PhotoId,
    val type: PhotoType,
    val storagePath: String
) {
    data class New(val photoId: PhotoId, val type: PhotoType, val storagePath: String)
}
