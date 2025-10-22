package ru.razornd.phototransit.commands

import org.springframework.shell.command.annotation.Command
import org.springframework.shell.command.annotation.Option
import ru.razornd.phototransit.OriginStoreService
import java.util.*

@Command(command = ["origin-store"], group = "Origin Store")
class OriginStoreCommands(private val service: OriginStoreService) {

    @Command(command = ["upload"], description = "Upload photo to origin store")
    fun upload(
        files: List<String>,
        @Option(
            longNames = ["original"],
            required = false,
            defaultValue = "false"
        )
        original: Boolean
    ) {
        val ids = mutableListOf<UUID>()

        for (file in files) {

            val id = service.uploadPhoto(file, original)

            ids.add(id)
        }

        println("Created photos: ${ids.joinToString(", ")}")
    }
}
