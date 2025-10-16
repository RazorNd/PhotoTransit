package ru.razornd.phototransit.util

import org.springframework.stereotype.Component
import java.io.InputStream
import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream

@Component
class TemporaryFileProcessor {

    fun process(inputStream: InputStream, prefix: String = "", suffix: String = "", block: (Path) -> Unit) {
        val tempFile = createTempFile(prefix, suffix)
        try {
            inputStream.transferTo(tempFile.outputStream())

            block(tempFile)
        } finally {
            tempFile.deleteIfExists()
        }
    }

}
