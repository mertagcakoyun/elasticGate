package com.example.application.elasticQuery.service

import com.couchbase.client.java.ReactiveCluster
import com.couchbase.client.java.ReactiveCollection
import com.couchbase.client.java.kv.ExistsResult
import com.example.application.connection.service.ConnectionService
import com.example.application.connection.service.ElasticConnection
import com.example.application.elasticQuery.ElasticsearchQueryRequest
import com.example.application.elasticQuery.QueryCouchbaseDocument
import com.example.application.elasticQuery.QueryRequest
import com.example.application.elasticQuery.QueryRequestWithSpecificHost
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import reactor.util.retry.Retry
import java.time.Duration

class QueryStorageService(
    private val reactiveCollection: ReactiveCollection,
    private val reactiveCluster: ReactiveCluster,
    private val connectionService: ConnectionService,
) {
    private val cbQuery =
        "SELECT c.* FROM Exposer c WHERE c.type = \"query\""
    private val logger = LoggerFactory.getLogger(javaClass)
    suspend fun save(request: QueryRequest, type: String = "query") {
        val key = createKey(request.name, type)
        val existsResult: ExistsResult = reactiveCollection.exists(key).awaitSingle()
        if (!existsResult.exists()) {
            val document = QueryCouchbaseDocument(request)
            reactiveCollection.insert(key, document).awaitSingle()
            logger.info("Query created for name: ${request.name} connection: ${request.connectionName}")
        } else {
            throw IllegalArgumentException("Query already exist with specified name : ${request.name}")
        }
    }

    suspend fun saveWithSpecificHost(request: QueryRequestWithSpecificHost, type: String = "query") {
        val key = createKey(request.name, type)
        checkQueryIsExistByName(key, request)
        val hostsWithConnectionName = getAllHostByConnectionName()

        val isHostExist = hostsWithConnectionName.any { hostWithName ->
            request.hosts.any { host -> hostWithName.hosts.contains(host) }
        }
        if (isHostExist) {
            val foundHostWithName = hostsWithConnectionName.first { hostWithName ->
                request.hosts.any { host -> hostWithName.hosts.contains(host) }
            }
            val newCommand = QueryRequest(
                request.name,
                foundHostWithName.name,
                request.index,
                request.query,
                request.dynamicParameters,
            )
            val document = QueryCouchbaseDocument(newCommand)
            reactiveCollection.insert(key, document).awaitSingle()
        } else {
            connectionService.saveConnection(
                ElasticConnection(
                    request.connectionName,
                    request.hosts,
                    request.port,
                ),
            )
            val queryRequest = QueryRequest(
                request.name,
                request.connectionName,
                request.index,
                request.query,
                request.dynamicParameters,
            )
            save(queryRequest)
        }
    }

    suspend fun delete(name: String, type: String = "query") {
        val key = createKey(name, type)
        val existsResult: ExistsResult = reactiveCollection.exists(key).awaitSingle()
        if (existsResult.exists()) {
            try {
                reactiveCollection.remove(key).awaitSingle()
            } catch (ex: Exception) {
                throw RuntimeException("Query could not removed name : $name")
            }
        } else {
            throw NoSuchElementException("There isn't query with specified name : $name")
        }
    }

    suspend fun update(name: String, queryRequest: QueryRequest) {
        val key = createKey(name = queryRequest.name, type = "query")
        val existsResult: ExistsResult = reactiveCollection.exists(key).awaitSingle()
        if (name != queryRequest.name) throw IllegalArgumentException("query name not included by request. queryName: $name, request.name: ${queryRequest.name}")
        if (existsResult.exists()) {
            val document = QueryCouchbaseDocument(queryRequest)
            reactiveCollection.upsert(key, document).awaitSingle()
        } else {
            throw IllegalArgumentException("There is a query with specified name : $name")
        }
    }

    suspend fun get(name: String): ElasticsearchQueryRequest {
        val key = createKey(name, "query")
        val retrySpec = Retry.backoff(2L, Duration.ofMillis(50))
        return reactiveCollection.get(key)
            .retryWhen(retrySpec)
            .doOnError { println("An error occurred while fetching a elastic query. id:$name") }
            .map { it.contentAs(ElasticsearchQueryRequest::class.java) }
            .awaitFirstOrNull() ?: throw Exception("query.not.found")
    }

    suspend fun getAll(): MutableList<QueryCouchbaseDocument> {
        val queryResult =
            reactiveCluster.query(cbQuery).awaitSingle() ?: throw NoSuchElementException("Query does not exist")
        val json = Json { ignoreUnknownKeys = true }

        return queryResult.rowsAsObject()
            .map { row -> json.decodeFromString<QueryCouchbaseDocument>(row.toString()) }
            .collectList()
            .awaitSingle()
    }

    private suspend fun getAllHostByConnectionName(): List<HostWithName> {
        val connections = connectionService.getAllConnection()
        return connections.map { HostWithName(it.name, it.hosts) }
    }

    private suspend fun checkQueryIsExistByName(
        key: String,
        request: QueryRequestWithSpecificHost,
    ) {
        val existsResult: ExistsResult = reactiveCollection.exists(key).awaitSingle()
        if (existsResult.exists()) {
            throw IllegalArgumentException("Query already exist with specified name : ${request.name}")
        }
    }

    private fun createKey(name: String, type: String) = "$type:$name"
}

data class HostWithName(val name: String, val hosts: List<String>)
