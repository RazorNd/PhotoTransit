package ru.razornd.phototransit.storage

import org.assertj.core.api.Assertions.assertThat
import org.assertj.db.api.Assertions.assertThat
import org.assertj.db.type.AssertDbConnection
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.jdbc.Sql
import ru.razornd.phototransit.PhotoId
import ru.razornd.phototransit.PostgresTestcontainersConfiguration
import ru.razornd.phototransit.UserId
import ru.razornd.phototransit.model.Photo
import ru.razornd.phototransit.model.PhotoFile
import ru.razornd.phototransit.service.PhotoType
import java.sql.Timestamp
import java.time.Instant

@JdbcTest
@Import(JdbcPhotoRepository::class, PostgresTestcontainersConfiguration::class, AssertjDbConfiguration::class)
open class JdbcPhotoRepositoryTest {

    @Autowired
    lateinit var photoRepository: JdbcPhotoRepository

    @Test
    fun upsert(@Autowired connection: AssertDbConnection) {
        val new = Photo.New(
            UserId("2e349e87-225f-4e67-baa2-20bd258a50f0"),
            "photo",
            Instant.parse("2013-09-04T16:58:00Z")
        )
        val changes = connection.changes().table("photos").build()

        val actual = changes.capture { photoRepository.upsert(new) }

        assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(new)

        assertThat(actual.id).isNotNull()

        assertThat(changes)
            .hasNumberOfChanges(1)
            .change()
            .isCreation()
            .rowAtEndPoint()
            .value("id").isEqualTo(actual.id.id)
            .value("owner_id").isEqualTo(new.owner.id)
            .value("name").isEqualTo(new.name)
            .value("created_at").isEqualTo(Timestamp.from(new.createdAt))
    }

    @Test
    @Sql(statements = ["INSERT INTO photos VALUES ('b4836fbb-a1fb-48e5-adb6-953ec6c79d2e', '2e349e87-225f-4e67-baa2-20bd258a50f0', 'my photo', '2013-09-10T16:58:00Z'::timestamptz)"])
    fun `upsert already exists`(@Autowired connection: AssertDbConnection) {
        val new = Photo.New(
            UserId("2e349e87-225f-4e67-baa2-20bd258a50f0"),
            "my photo",
            Instant.parse("2013-09-04T16:58:00Z")
        )

        val changes = connection.changes().table("photos").build()

        val actual = changes.capture { photoRepository.upsert(new) }

        assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(new)

        assertThat(actual.id).isEqualTo(PhotoId("b4836fbb-a1fb-48e5-adb6-953ec6c79d2e"))

        assertThat(changes)
            .hasNumberOfChanges(1)
            .change()
            .isModification()
            .rowAtEndPoint()
            .value("id").isEqualTo(actual.id.id)
            .value("owner_id").isEqualTo(new.owner.id)
            .value("name").isEqualTo(new.name)
            .value("created_at").isEqualTo(Timestamp.from(new.createdAt))
    }

    @Test
    @Sql(statements = ["INSERT INTO photos VALUES ('597e243d-002a-4a23-8540-f54d30fb664a', 'b4836fbb-a1fb-48e5-adb6-953ec6c79d2e', 'photo', '2003-08-08T05:53:29Z'::timestamptz)"])
    fun saveFile(@Autowired connection: AssertDbConnection) {
        val new = PhotoFile.New(
            PhotoId("597e243d-002a-4a23-8540-f54d30fb664a"),
            PhotoType.PROCESSED,
            "path/to/file.tiff"
        )

        val changes = connection.changes().table("photo_files").build()

        val actual = changes.capture { photoRepository.saveFile(new) }

        assertThat(actual)
            .usingRecursiveComparison()
            .ignoringFields("id")
            .isEqualTo(new)

        assertThat(changes)
            .hasNumberOfChanges(1)
            .change()
            .isCreation()
            .rowAtEndPoint()
            .value("id").isEqualTo(actual.id)
            .value("photo_id").isEqualTo(actual.photoId.id)
            .value("type").isEqualTo(actual.type.name)
            .value("storage_path").isEqualTo(actual.storagePath)
    }
}
