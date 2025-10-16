package ru.razornd.phototransit.storage.file

import java.nio.file.Path


interface FileStorage {
    fun saveFile(storagePath: String, file: Path)
}
