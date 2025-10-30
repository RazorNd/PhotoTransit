package ru.razornd.phototransit.image

import com.ashampoo.kim.format.ImageMetadata
import com.ashampoo.kim.format.tiff.constant.ExifTag
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private val exifDatetimePattern = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss.SSS")

fun ImageMetadata.originalDateTime(): Instant? {
    val dateTime = findStringValue(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL) ?: return null
    val subSeconds = findStringValue(ExifTag.EXIF_TAG_SUB_SEC_TIME_ORIGINAL) ?: "0"
    val offset = findStringValue(ExifTag.EXIF_TAG_OFFSET_TIME_ORIGINAL)?.let { ZoneOffset.of(it) } ?: ZoneOffset.UTC

    return LocalDateTime.parse("$dateTime.$subSeconds", exifDatetimePattern).toInstant(offset)
}
