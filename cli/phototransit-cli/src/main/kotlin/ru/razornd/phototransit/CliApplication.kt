package ru.razornd.phototransit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.shell.command.annotation.CommandScan

@CommandScan("ru.razornd.phototransit.commands")
@SpringBootApplication
open class CliApplication

fun main(args: Array<String>) {
    runApplication<CliApplication>(*args)
}
