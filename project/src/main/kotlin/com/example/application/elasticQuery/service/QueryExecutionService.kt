package com.example.application.elasticQuery.service

import co.elastic.clients.json.JsonData
import com.example.application.connection.service.ConnectionService
import com.example.application.elasticQuery.ExecuteExistElasticsearchQueryRequest
import com.example.infrastructure.boilerplate.elasticsearch.RestClientFactory

class QueryExecutionService(
    private val elasticsearchQueryService: ElasticsearchQueryService,
    private val queryStorageService: QueryStorageService,
    private val connectionService: ConnectionService,
    private val restClientFactory: RestClientFactory,
) {

    suspend fun execute(request: ExecuteExistElasticsearchQueryRequest): JsonData {
        val elasticsearchQueryRequest = queryStorageService.get(request.name)
        val elasticsearchQueryString =
            elasticsearchQueryService.updateQueryWithParameters(elasticsearchQueryRequest, request.queryParams)
        val connectionInformation = connectionService.getConnection(request.connectionName)
        val restClientWrapper = restClientFactory.createRestClientWrapper(
            connectionInformation.hosts,
            connectionInformation.port,
        )
        try {
            val responseBody = elasticsearchQueryService.sendRequestToElasticsearchAsync(
                elasticsearchQueryRequest,
                elasticsearchQueryString,
                restClientWrapper,
            ).get()
            return JsonData.fromJson(responseBody)
        } finally {
            restClientWrapper.closeClient()
        }
    }
}
