package ru.razornd.phototransit.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.nio.file.Path
import kotlin.io.path.createTempFile
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream

@SpringBootTest(classes = [TemporaryFileProcessor::class])
class TemporaryFileProcessorTest {

    @Autowired
    lateinit var processor: TemporaryFileProcessor

    @Test
    fun process(@TempDir tempDir: Path) {
        val tempFile = createTempFile(tempDir, "test", ".txt")
        var actual: Path? = null

        processor.process("test".byteInputStream()) {
            it.inputStream().transferTo(tempFile.outputStream())
            actual = it
        }

        assertThat(tempFile).hasContent("test")

        assertThat(actual)
            .describedAs("Process should delete processed temporary file")
            .isNotNull
            .doesNotExist()
    }
}
