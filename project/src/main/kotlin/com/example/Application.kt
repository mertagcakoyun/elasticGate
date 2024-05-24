package com.example

import com.example.infrastructure.boilerplate.ktor.configureCORS
import com.example.infrastructure.boilerplate.ktor.configureHTTP
import com.example.infrastructure.boilerplate.ktor.routing.configureRouting
import com.example.infrastructure.boilerplate.modules.couchbaseModule
import com.example.infrastructure.boilerplate.modules.mainModule
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    embeddedServer(
        Netty,
        commandLineEnvironment(args),
    ).start(wait = true)
}

fun Application.configure() {
    module { single { environment } }
    install(Koin) {
        modules(
            couchbaseModule(),
            mainModule
        )
    }
    install(ContentNegotiation) {
        json(
            Json {
                encodeDefaults = true
                ignoreUnknownKeys = true
                coerceInputValues = true
            },
        )
    }
    configureHTTP()
    configureCORS()
    configureRouting()
}
