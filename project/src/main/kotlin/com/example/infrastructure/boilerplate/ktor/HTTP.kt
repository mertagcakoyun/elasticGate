package com.example.infrastructure.boilerplate.ktor

import com.example.infrastructure.boilerplate.config.configureSwagger
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.defaultheaders.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

const val USER_EMAIL_KEY: String = "x-useremail"
const val CORRELATION_ID_KEY: String = "x-correlationId"
const val CONTACT_ID_KEY: String = "x-contactId"
const val ACCOUNT_ID_KEY: String = "x-accountId"
const val APP_NAME = "BrandCenter"
const val AGENT_NAME_KEY = "x-agentname"
const val CREATED_DATE_KEY = "x-createddate"
const val EXECUTOR_CONTACT_NAME = "x-executor-contact-name"
const val EXECUTOR_CONTACT_SURNAME = "x-executor-contact-surname"

fun Application.configureCORS() {
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Delete)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.UserAgent)
        allowHeader(HttpHeaders.Referrer)
        allowHeader(HttpHeaders.Accept)
        allowHeader("X-Useremail")
        allowHeader(HttpHeaders.ContentType)
        allowHeader("Access-Control-Allow-Headers")
        anyHost()
    }
    install(DefaultHeaders) {
        // header("X-Engine", "Ktor") // will send this header with each response
    }
}

fun Application.configureHTTP() {
    configureSwagger()
}

fun Application.configureDBOptions(){

}