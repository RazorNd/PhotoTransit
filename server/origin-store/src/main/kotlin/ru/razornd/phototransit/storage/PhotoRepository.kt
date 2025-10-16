package ru.razornd.phototransit.storage

import org.springframework.stereotype.Repository
import ru.razornd.phototransit.model.Photo
import ru.razornd.phototransit.model.PhotoFile

interface PhotoRepository {
    fun upsert(new: Photo.New): Photo
    fun saveFile(new: PhotoFile.New): PhotoFile
}

@Repository
class JdbcPhotoRepository : PhotoRepository {

    override fun upsert(new: Photo.New): Photo {
        TODO("Not yet implemented")
    }

    override fun saveFile(new: PhotoFile.New): PhotoFile {
        TODO("Not yet implemented")
    }
}
