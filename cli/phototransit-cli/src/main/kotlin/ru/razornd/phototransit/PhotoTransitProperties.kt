package ru.razornd.phototransit

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "phototransit")
data class PhotoTransitProperties(val baseUrl: String)
