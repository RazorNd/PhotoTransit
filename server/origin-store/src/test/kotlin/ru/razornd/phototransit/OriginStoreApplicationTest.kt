package ru.razornd.phototransit

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestcontainersConfiguration::class, MinioTestcontainersConfiguration::class)
class OriginStoreApplicationTest {


    @Test
    fun loadContext() {
    }

}
