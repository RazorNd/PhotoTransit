package ru.razornd.phototransit

import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import java.nio.file.Files
import java.nio.file.Path

@Component
class MediaTypeResolver {

    fun resolve(file: Path): MediaType? {
        return Files.probeContentType(file)?.let { MediaType.parseMediaType(it) }
    }

}
