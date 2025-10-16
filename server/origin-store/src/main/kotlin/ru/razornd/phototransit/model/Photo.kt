package ru.razornd.phototransit.model

import ru.razornd.phototransit.PhotoId
import ru.razornd.phototransit.UserId
import java.time.Instant

data class Photo(
    val id: PhotoId,
    val owner: UserId,
    val name: String,
    val createdAt: Instant
) {
    data class New(val owner: UserId, val name: String, val createdAt: Instant)
}
