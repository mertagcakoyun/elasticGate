package com.example.infrastructure.boilerplate.config

import com.example.infrastructure.boilerplate.utils.getEnvironment
import io.ktor.server.config.*

fun ApplicationConfig.getOrNull(key: String): ApplicationConfigValue? {
    val environment = getEnvironment()
    return propertyOrNull("$environment.$key")
}

fun ApplicationConfig.get(key: String): ApplicationConfigValue {
    val environment = getEnvironment()
    return property("$environment.$key")
}

fun ApplicationConfig.getConfig(key: String): ApplicationConfig {
    val environment = getEnvironment()
    return config("$environment.$key")
}



