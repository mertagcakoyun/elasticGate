package com.example.infrastructure.boilerplate.ktor.routing

import com.example.infrastructure.boilerplate.ktor.USER_EMAIL_KEY
import com.example.infrastructure.components.connection.routing.configureConnectionRouting
import com.example.infrastructure.components.elasticQuery.routing.configureQueryExposerRouting
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        configureHealthcheckRouting()
        configureQueryExposerRouting()
        configureConnectionRouting()
        configureInterceptors()
    }
}

private fun Routing.configureInterceptors() {
    intercept(ApplicationCallPipeline.Call) {
        if (call.request.path() == "/contact-login" ||
            call.request.path() == "/contact-reset-password" ||
            call.request.path() == "/contact-forgot-password" ||
            call.request.path() == "/admin-login" ||
            call.request.path() == "/" ||
            call.request.path() == "/swagger" ||
            call.request.path() == "/openapi.json" ||
            call.request.path().contains("/query/") ||
            call.request.httpMethod == HttpMethod.Get ||
            call.request.httpMethod == HttpMethod.Options ||
            call.request.httpMethod == HttpMethod.Head
        ) {
            return@intercept
        }

        if (!call.request.headers.contains(USER_EMAIL_KEY)) {
            call.respond(HttpStatusCode.BadRequest, "$USER_EMAIL_KEY header is required for all ")
            return@intercept finish()
        }
    }
}

private fun Routing.configureHealthcheckRouting() {
    route("health") {
        get("") {
            call.respond(HttpStatusCode.OK)
        }
    }
}
