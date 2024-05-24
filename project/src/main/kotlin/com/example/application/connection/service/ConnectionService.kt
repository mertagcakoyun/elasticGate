package com.example.application.connection.service

import com.couchbase.client.java.ReactiveCluster
import com.couchbase.client.java.ReactiveCollection
import com.couchbase.client.java.kv.ExistsResult
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import reactor.util.retry.Retry
import java.time.Duration

class ConnectionService(
    private val reactiveCollection: ReactiveCollection,
    private val reactiveCluster: ReactiveCluster,
) {
    private val cbQuery =
        "SELECT c.* FROM Exposer c WHERE c.type = \"connection\""

    suspend fun removeConnection(name: String) {
        val key = createConnectionKey(name)
        val existsResult: ExistsResult = reactiveCollection.exists(key).awaitSingle()
        if (existsResult.exists()) {
            try {
                reactiveCollection.remove(key).awaitSingle()
            } catch (ex: Exception) {
                throw RuntimeException("Connection could not removed name : $name")
            }
        } else {
            throw NoSuchElementException("There isn't connection with specified name : $name")
        }
    }

    suspend fun saveConnection(connection: ElasticConnection) {
        val key = createConnectionKey(connection.name)
        val existsResult: ExistsResult = reactiveCollection.exists(key).awaitSingle()
        if (!existsResult.exists()) {
            reactiveCollection.insert(key, connection).awaitSingle()
        } else {
            throw IllegalArgumentException("There is a connection with specified name : ${connection.name}")
        }
    }

    suspend fun getConnection(name: String): ElasticConnection {
        val key = createConnectionKey(name)
        val retrySpec = Retry.backoff(2L, Duration.ofMillis(50))
        return reactiveCollection.get(key)
            .retryWhen(retrySpec)
            .map { it.contentAs(ElasticConnection::class.java) }
            .awaitFirst()
            ?: throw IllegalArgumentException("There is not any connection with this name. name : $name")
    }

    suspend fun getAllConnection(): List<ElasticConnection> {
        val queryResult = reactiveCluster.query(cbQuery).awaitSingle()
        val json = Json { ignoreUnknownKeys = true }

        return queryResult.rowsAsObject()
            .map { row -> json.decodeFromString<ElasticConnection>(row.toString()) }
            .collectList()
            .awaitSingle()
    }
}

private fun createConnectionKey(name: String) = "connection:$name"

@Serializable
data class ElasticConnection(
    val name: String,
    val hosts: List<String>,
    val port: Int,
    val type: String = "connection",
)

@Serializable
data class ConnectionCreateRequest(val name: String, val hosts: List<String>, val port: Int)
