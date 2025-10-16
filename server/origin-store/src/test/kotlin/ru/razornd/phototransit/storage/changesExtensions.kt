package ru.razornd.phototransit.storage

import org.assertj.db.type.Changes

fun <T> Changes.capture(supplier: () -> T): T {
    this.setStartPointNow()
    val result = supplier()
    this.setEndPointNow()
    return result
}
