package ru.razornd.phototransit.storage

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import ru.razornd.phototransit.PhotoId
import ru.razornd.phototransit.UserId
import ru.razornd.phototransit.model.Photo
import ru.razornd.phototransit.model.PhotoFile
import ru.razornd.phototransit.service.PhotoType
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.*

interface PhotoRepository {
    fun upsert(new: Photo.New): Photo
    fun saveFile(new: PhotoFile.New): PhotoFile
}

@Repository
class JdbcPhotoRepository(private val jdbcClient: JdbcClient) : PhotoRepository {

    // language=sql
    private val upsertPhotoSql = """
        INSERT INTO photos (owner_id, name, created_at)
        VALUES (:ownerId, :name, :createdAt)
        ON CONFLICT (owner_id, name) DO UPDATE 
        SET created_at = LEAST(photos.created_at, EXCLUDED.created_at)
        RETURNING id, owner_id, name, created_at
    """.trimIndent()

    // language=sql
    private val insertPhotoFileSql = """
        INSERT INTO photo_files (photo_id, type, storage_path)
        VALUES (:photoId, :type, :storagePath)
        RETURNING id, photo_id, type, storage_path
    """.trimIndent()

    override fun upsert(new: Photo.New): Photo {
        return jdbcClient.sql(upsertPhotoSql)
            .param("ownerId", new.owner.id)
            .param("name", new.name)
            .param("createdAt", Timestamp.from(new.createdAt))
            .query(PhotoRowMapper)
            .single()
    }

    override fun saveFile(new: PhotoFile.New): PhotoFile {
        return jdbcClient.sql(insertPhotoFileSql)
            .param("photoId", new.photoId.id)
            .param("type", new.type.name)
            .param("storagePath", new.storagePath)
            .query(PhotoFileRowMapper)
            .single()
    }

    object PhotoRowMapper : RowMapper<Photo> {

        override fun mapRow(resultSet: ResultSet, rowNum: Int) = Photo(
            id = PhotoId(resultSet.getObject("id", UUID::class.java)),
            owner = UserId(resultSet.getObject("owner_id", UUID::class.java)),
            name = resultSet.getString("name"),
            createdAt = resultSet.getTimestamp("created_at").toInstant()
        )

    }

    object PhotoFileRowMapper : RowMapper<PhotoFile> {

        override fun mapRow(resultSet: ResultSet, rowNum: Int) = PhotoFile(
            id = resultSet.getObject("id", UUID::class.java),
            photoId = PhotoId(resultSet.getObject("photo_id", UUID::class.java)),
            type = PhotoType.valueOf(resultSet.getString("type")),
            storagePath = resultSet.getString("storage_path")
        )

    }
}
