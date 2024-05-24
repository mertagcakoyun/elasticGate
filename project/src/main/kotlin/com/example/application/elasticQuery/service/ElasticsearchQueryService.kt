package com.example.application.elasticQuery.service

import com.example.application.elasticQuery.DynamicParameter
import com.example.application.elasticQuery.ElasticsearchQueryRequest
import com.example.application.elasticQuery.QueryParams
import com.example.infrastructure.boilerplate.elasticsearch.IRestClientWrapper
import org.elasticsearch.client.Request
import org.elasticsearch.client.Response
import org.elasticsearch.client.ResponseListener
import java.util.concurrent.CompletableFuture

class ElasticsearchQueryService {

    fun updateQueryWithParameters(dbResponse: ElasticsearchQueryRequest, queryParams: QueryParams): String {
        var query = dbResponse.request.query
        val parametersWillUseDefault = mutableListOf<DynamicParameter>()
        getUnspecifiedDynamicParameters(dbResponse, queryParams, parametersWillUseDefault)
        if (parametersWillUseDefault.size > 0) {
            parametersWillUseDefault.forEach { parameter ->
                if (parameter.type != "combo") {
                    query = query.replace("\$${parameter.name}", parameter.default.orEmpty())
                } else {
                    query = query.replace("\$${parameter.name}", parameter.values!!.first().toString())
                }
            }
        }
        queryParams.parameters.forEach { (key, value) ->
            query = query.replace("\$$key", value)
        }
        return query
    }

    private fun getUnspecifiedDynamicParameters(
        dbResponse: ElasticsearchQueryRequest,
        queryParams: QueryParams,
        parametersWillUseDefault: MutableList<DynamicParameter>,
    ) {
        if (dbResponse.request.dynamicParameters.size != queryParams.parameters.size) {
            dbResponse.request.dynamicParameters.forEach { param ->
                if (!queryParams.parameters.contains(param.name)) {
                    parametersWillUseDefault.add(param)
                }
            }
        }
    }

    fun sendRequestToElasticsearchAsync(
        elasticsearchQueryRequest: ElasticsearchQueryRequest,
        elasticsearchQueryString: String,
        restClientWrapper: IRestClientWrapper,
    ): CompletableFuture<String> {
        val future = CompletableFuture<String>()
        val request = Request("GET", "/${elasticsearchQueryRequest.request.index}/_search")
        request.setJsonEntity(elasticsearchQueryString)
        restClientWrapper.performRequestAsync(
            request,
            object : ResponseListener {
                override fun onSuccess(response: Response) {
                    val responseBody = response.entity?.content?.reader()?.readText()
                    future.complete(responseBody)
                }

                override fun onFailure(exception: Exception) {
                    future.completeExceptionally(exception)
                }
            },
        )
        return future
    }
}
