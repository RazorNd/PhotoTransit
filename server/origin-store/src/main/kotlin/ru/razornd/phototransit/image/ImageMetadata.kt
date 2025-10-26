package ru.razornd.phototransit.image

import java.nio.file.Path
import java.time.Instant

interface ImageMetadata {

    val createDate: Instant

}

interface ImageMetadataReader {

    fun readMetaData(path: Path): ImageMetadata

}

