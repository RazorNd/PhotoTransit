package ru.razornd.phototransit

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.http.MediaType
import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeBytes

class MediaTypeResolverTest {

    private val resolver = MediaTypeResolver()

    @ParameterizedTest
    @MethodSource("files")
    fun resolve(fileSuffix: String, content: ByteArray, expectedMediaType: MediaType, @TempDir tempDir: Path) {
        val path = createTempFile(directory = tempDir, suffix = fileSuffix).apply {
            writeBytes(content)
        }

        assertThat(resolver.resolve(path)).isEqualTo(expectedMediaType)

        path.deleteIfExists()
    }

    companion object {
        @JvmStatic
        fun files(): Collection<Arguments> = listOf(
            Arguments.of(
                ".jpg",
                byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte()),
                MediaType.IMAGE_JPEG
            ),
            Arguments.of(
                ".png",
                byteArrayOf(0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte()),
                MediaType.IMAGE_PNG
            ),
            Arguments.of(
                ".tiff",
                byteArrayOf(0x49, 0x49, 0x2A, 0x00),
                MediaType.parseMediaType("image/tiff")
            )
        )
    }
}
