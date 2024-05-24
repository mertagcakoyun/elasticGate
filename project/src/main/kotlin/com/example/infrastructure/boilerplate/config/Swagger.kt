package com.example.infrastructure.boilerplate.config

import com.example.infrastructure.boilerplate.ktor.CONTACT_ID_KEY
import com.example.infrastructure.boilerplate.ktor.USER_EMAIL_KEY
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.PathItem.HttpMethod.*
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.parser.OpenAPIV3Parser

fun Application.configureSwagger() {
    val serverUrl = environment.config.get("server.url").getString()

    routing {
        val openApi = OpenAPIV3Parser().read("openapi/documentation.yaml")

        openApi.info.title = "ES Exposer Service"

        val server = Server().apply { url = serverUrl }
        val local = Server().apply { url = "http://0.0.0.0:8080" }
        openApi.servers = listOf(server, local)

        // Update content type
        openApi.paths.values.forEach { pathItem ->
            val operations = pathItem.readOperationsMap()

            listOf(GET, POST, PUT, PATCH).forEach methods@{ method ->
                val operation = operations[method]
                if (operation != null) {
                    operation.addParametersItem(
                        Parameter().apply {
                            `in` = "header"
                            name = USER_EMAIL_KEY
                        },
                    )

                    operation.addParametersItem(
                        Parameter().apply {
                            `in` = "header"
                            name = CONTACT_ID_KEY
                        },
                    )

                    if (operation.requestBody == null) {
                        return@methods
                    }

                    val existingContent = operation.requestBody.content
                    val newContent = Content()
                    existingContent.values.forEach {
                        newContent.addMediaType("application/json", it)
                    }
                    operation.requestBody.content = newContent
                }
            }
        }

        val updatedOpenApi = Json.pretty().writeValueAsString(openApi)

        swaggerUI(path = "openapi", "documentation.json", updatedOpenApi)

        get("/") {
            call.respondRedirect("/openapi", permanent = true)
        }
    }
}
