package ru.razornd.phototransit

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import java.nio.file.Files
import kotlin.io.path.createTempFile
import kotlin.io.path.writeBytes

class MediaTypeResolverTest {
    @Test
    fun resolve() {
        val resolver = MediaTypeResolver()

        val jpeg = createTempFile(suffix = ".jpg").apply {
            writeBytes(arrayOf(0xFF, 0xD8, 0xFF).map { it.toByte() }.toByteArray())
        }
        val png = createTempFile(suffix = ".png").apply {
            writeBytes(
                byteArrayOf(
                    0x89.toByte(),
                    0x50.toByte(),
                    0x4E.toByte(),
                    0x47.toByte()
                )
            )
        }
        val raw = createTempFile(suffix = ".arw").apply { writeBytes(byteArrayOf(0x49, 0x49, 0x2A, 0x00)) }

        assertThat(resolver.resolve(jpeg)).isEqualTo(MediaType.IMAGE_JPEG)
        assertThat(resolver.resolve(png)).isEqualTo(MediaType.IMAGE_PNG)
        assertThat(resolver.resolve(raw)).isEqualTo(MediaType.parseMediaType("image/x-sony-arw"))

        Files.deleteIfExists(jpeg)
        Files.deleteIfExists(png)
        Files.deleteIfExists(raw)
    }
}
