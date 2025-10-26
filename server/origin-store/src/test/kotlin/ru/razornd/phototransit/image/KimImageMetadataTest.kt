package ru.razornd.phototransit.image

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.net.URL
import java.nio.file.Path
import java.time.Instant

class KimImageMetadataTest {

    private val metadataReader = KimImageMetadata()

    @CsvSource("jpeg", "heic", "png", "tiff")
    @ParameterizedTest
    fun readMetaData(extension: String) {
        val path = Path.of(image(extension).toURI())
        val metaData = metadataReader.readMetaData(path)

        assertThat(metaData.createDate).isEqualTo(Instant.parse("2025-10-03T14:37:33.139Z"))
    }

    private fun image(extension: String): URL = checkNotNull(
        javaClass.getResource("/test-image.$extension")
    ) { "Test image 'test-image.$extension' not found" }
}
