package com.example.infrastructure.components.connection.routing

import com.example.application.connection.service.ConnectionCreateRequest
import com.example.application.connection.service.ConnectionService
import com.example.application.connection.service.ElasticConnection
import com.trendyol.kediatr.Mediator
import io.ktor.http.*
import io.ktor.network.sockets.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject

/**
 * @OpenAPITag Reports
 */
fun Routing.configureConnectionRouting() {
    val connectionService: ConnectionService by inject()
    route("connections") {
        post("/connection") {
            val request = call.receive<ConnectionCreateRequest>()
            try {
                connectionService.saveConnection(
                    ElasticConnection(
                        name = request.name,
                        hosts = request.hosts,
                        port = request.port,
                    ),
                )
                call.respond(HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message.toString())
            }
        }

        get("/connection-by-name/{name}") {
            try {
                val name = call.parameters.getOrFail("name")

                val queryResponse = connectionService.getConnection(name)
                call.respondText(
                    Json.encodeToString(ElasticConnection.serializer(), queryResponse),
                    ContentType.Application.Json,
                    HttpStatusCode.OK,
                )
            } catch (ex: Exception) {
                call.respond(HttpStatusCode.BadRequest, ex.message.toString())
            }
        }

        get() {
            try {
                val queryResponse = connectionService.getAllConnection()
                val connectionResponse = ConnectionResponse(queryResponse.map { it })
                call.respondText(
                    Json.encodeToString(ConnectionResponse.serializer(), connectionResponse),
                    ContentType.Application.Json,
                    HttpStatusCode.OK,
                )
            } catch (ex: Exception) {
                call.respond(HttpStatusCode.BadRequest, ex.message.toString())
            }
        }

        delete("/connection/{name}") {
            try {
                val name = call.parameters.getOrFail("name")
                connectionService.removeConnection(name)
                call.respond(HttpStatusCode.NoContent)
            } catch (ex: Exception) {
                call.respond(HttpStatusCode.BadRequest, ex.message.toString())
            }
        }
    }
}

@Serializable
data class ConnectionResponse(val connections: List<ElasticConnection>)
