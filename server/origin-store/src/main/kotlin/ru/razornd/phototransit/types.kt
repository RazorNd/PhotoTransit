package ru.razornd.phototransit

import java.util.*

@JvmInline
value class PhotoId(val id: UUID) {
    constructor(id: String) : this(UUID.fromString(id))

    override fun toString() = id.toString()
}

@JvmInline
value class UserId(val id: UUID) {
    constructor(id: String) : this(UUID.fromString(id))
}
