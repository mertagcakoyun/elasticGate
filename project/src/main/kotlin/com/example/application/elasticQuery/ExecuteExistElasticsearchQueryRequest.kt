package com.example.application.elasticQuery

import kotlinx.serialization.Serializable

@Serializable
data class ExecuteExistElasticsearchQueryRequest(
    val name: String,
    val connectionName: String,
    val queryParams: QueryParams,
)

@Serializable
data class QueryParams(
    val parameters: Map<String, String>,
)

@Serializable
data class QueryRequestWithSpecificHost(
    val name: String,
    val connectionName: String,
    val hosts: List<String>,
    val port: Int,
    val index: String,
    val query: String,
    val dynamicParameters: List<DynamicParameter>,
)

@Serializable
data class QueryRequest(
    val name: String,
    val connectionName: String,
    val index: String,
    val query: String,
    val dynamicParameters: List<DynamicParameter>,
)

@Serializable
data class DynamicParameter(
    val name: String,
    val type: String,
    val default: String? = null,
    val values: List<String>? = null,
)

@Serializable
data class QueryCouchbaseDocument(val request: QueryRequest, val type: String = "query")

@Serializable
data class ElasticsearchQueryRequest(val request: QueryRequest)