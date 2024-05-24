package com.example.infrastructure.boilerplate.ktor

import com.example.application.elasticQuery.QueryRequest
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

val jsonSerializersModule = SerializersModule {
    QueryRequest::class.java
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(
            Json {
                serializersModule = jsonSerializersModule
                encodeDefaults = true
                ignoreUnknownKeys = true
                coerceInputValues = true
            },
        )
    }
}
