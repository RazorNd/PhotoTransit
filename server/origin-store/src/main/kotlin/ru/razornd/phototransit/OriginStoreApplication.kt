package ru.razornd.phototransit

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class OriginStoreApplication

fun main(args: Array<String>) {
    runApplication<OriginStoreApplication>(*args)
}
