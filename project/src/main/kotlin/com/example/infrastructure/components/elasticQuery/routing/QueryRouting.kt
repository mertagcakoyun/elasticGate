package com.example.infrastructure.components.elasticQuery.routing

import com.example.application.elasticQuery.*
import com.example.application.elasticQuery.service.QueryExecutionService
import com.example.application.elasticQuery.service.QueryStorageService
import io.ktor.http.*
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
fun Routing.configureQueryExposerRouting() {
    val queryStorageService: QueryStorageService by inject()
    val queryExecutionService: QueryExecutionService by inject()
    route("queries") {
        post("/elastic-query") {
            try {
                val queryRequest = call.receive<QueryRequest>()
                queryStorageService.save(queryRequest)
                call.respond(HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message.toString())
            }
        }

        post("/elastic-query-with-specific-host") {
            try {
                val queryRequest = call.receive<QueryRequestWithSpecificHost>()
                queryStorageService.saveWithSpecificHost(queryRequest)
                call.respond(HttpStatusCode.Created)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, e.message.toString())
            }
        }
        get("/query-by-name/{name}") {
            try {
                val name = call.parameters.getOrFail("name")
                val queryResponse = queryStorageService.get(name)
                call.respondText(
                    Json.encodeToString(ElasticsearchQueryRequest.serializer(), queryResponse),
                    ContentType.Application.Json,
                    HttpStatusCode.OK,
                )
            } catch (ex: Exception) {
                call.respond(HttpStatusCode.BadRequest, ex.message.toString())
            }
        }
        get("/connections/{connectionName}/queries/{name}") {
            try {
                val queryParams = call.receive<QueryParams>()
                val name = call.parameters.getOrFail("name")
                val connectionName = call.parameters.getOrFail("connectionName")
                val queryResponse = queryExecutionService.execute(
                    ExecuteExistElasticsearchQueryRequest(
                        name,
                        connectionName,
                        queryParams,
                    ),
                )
                call.respondText(queryResponse.toString(), ContentType.Application.Json, HttpStatusCode.OK)
            } catch (ex: Exception) {
                call.respond(HttpStatusCode.BadRequest, ex.message.toString())
            }
        }

        get() {
            try {
                val queryResponse = queryStorageService.getAll()
                val queries = QueryResponse(queryResponse.map { it.request })
                call.respondText(
                    Json.encodeToString(QueryResponse.serializer(), queries),
                    ContentType.Application.Json,
                    HttpStatusCode.OK,
                )
            } catch (ex: Exception) {
                call.respond(HttpStatusCode.BadRequest, ex.message.toString())
            }
        }

        put("/query/{name}") {
            try {
                val queryRequest = call.receive<QueryRequest>()
                val name = call.parameters.getOrFail("name")
                queryStorageService.update(name, queryRequest)
                call.respond(HttpStatusCode.OK)
            } catch (ex: Exception) {
                call.respond(HttpStatusCode.BadRequest, ex.message.toString())
            }
        }

        delete("/query/{name}") {
            try {
                val name = call.parameters.getOrFail("name")
                queryStorageService.delete(name)
                call.respond(HttpStatusCode.NoContent)
            } catch (ex: Exception) {
                call.respond(HttpStatusCode.BadRequest, ex.message.toString())
            }
        }
    }
}

@Serializable
data class QueryResponse(val requests: List<QueryRequest>)
