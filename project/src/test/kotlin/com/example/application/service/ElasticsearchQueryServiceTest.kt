package com.example.application.service

import com.example.application.elasticQuery.DynamicParameter
import com.example.application.elasticQuery.ElasticsearchQueryRequest
import com.example.application.elasticQuery.QueryParams
import com.example.application.elasticQuery.QueryRequest
import com.example.application.elasticQuery.service.ElasticsearchQueryService
import com.example.infrastructure.boilerplate.elasticsearch.IRestClientWrapper
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.apache.http.HttpEntity
import org.elasticsearch.client.Response
import org.elasticsearch.client.ResponseListener
import java.io.ByteArrayInputStream

class ElasticsearchQueryServiceTest : FunSpec({
    val restClientWrapper = mockk<IRestClientWrapper>()
    val sut = ElasticsearchQueryService()

    test("updateQueryWithParameters should replace dynamic parameters correctly") {
        // Given
        val dbResponse = ElasticsearchQueryRequest(
            request = QueryRequest(
                name = "testQuery",
                connectionName = "testConnection",
                index = "testIndex",
                query = "{ \"size\": \$SIZE, \"query\": { \"term\": { \"status\": \"\$STATUS\" } } }",
                dynamicParameters = listOf(
                    DynamicParameter(name = "SIZE", type = "integer", default = "10"),
                    DynamicParameter(name = "STATUS", type = "string"),
                ),
            ),
        )
        val queryParams = QueryParams(
            parameters = mapOf("STATUS" to "ACTIVE"),
        )
        val expectedQuery = "{ \"size\": 10, \"query\": { \"term\": { \"status\": \"ACTIVE\" } } }"

        // When
        val updatedQuery = sut.updateQueryWithParameters(dbResponse, queryParams)

        // Then
        updatedQuery shouldBe expectedQuery
    }

    test("updateQueryWithParameters should replace dynamic parameters default values correctly if any paramater does not exist") {
        // Given
        val dbResponse = ElasticsearchQueryRequest(
            request = QueryRequest(
                name = "testQuery",
                connectionName = "testConnection",
                index = "testIndex",
                query = "{ \"size\": \$SIZE, \"query\": { \"term\": { \"status\": \"\$STATUS\" } } }",
                dynamicParameters = listOf(
                    DynamicParameter(name = "SIZE", type = "integer", default = "20"),
                    DynamicParameter(name = "STATUS", type = "string", default = "PASSIVE"),
                ),
            ),
        )
        val queryParams = QueryParams(
            parameters = mapOf(),
        )
        val expectedQuery = "{ \"size\": 20, \"query\": { \"term\": { \"status\": \"PASSIVE\" } } }"

        // When
        val updatedQuery = sut.updateQueryWithParameters(dbResponse, queryParams)

        // Then
        updatedQuery shouldBe expectedQuery
    }

    test("sendRequestToElasticsearchAsync should complete future with response") {
        // Gİven
        coEvery {
            restClientWrapper.performRequestAsync(any(), any<ResponseListener>())
        } answers {
            val listener = arg<ResponseListener>(1)
            val mockContent = "response body"
            val mockInputStream = ByteArrayInputStream(mockContent.toByteArray())
            val mockEntity = mockk<HttpEntity>()
            every { mockEntity.content } returns mockInputStream

            val response = mockk<Response>()
            every { response.entity } returns mockEntity

            listener.onSuccess(response)
        }
        val elasticsearchQueryRequest = ElasticsearchQueryRequest(
            request = QueryRequest(
                name = "testQuery",
                connectionName = "testConnection",
                index = "testIndex",
                query = "some query",
                dynamicParameters = emptyList(),
            ),
        )
        val elasticsearchQueryString = "some query"

        // When
        val future = sut.sendRequestToElasticsearchAsync(
            elasticsearchQueryRequest,
            elasticsearchQueryString,
            restClientWrapper,
        )
        val result = future.get()

        // Then
        result shouldBe "response body"
    }

    test("updateQueryWithParameters should use provided value for combo type") {
        // Given
        val dynamicParameters = listOf(
            DynamicParameter(name = "COLOR", type = "combo", values = listOf("red", "green", "blue")),
        )
        val queryParams = QueryParams(parameters = mapOf("COLOR" to "green"))
        val queryRequest = QueryRequest(
            name = "testQuery",
            connectionName = "testConnection",
            index = "testIndex",
            query = "color: \$COLOR",
            dynamicParameters = dynamicParameters,
        )
        val dbResponse = ElasticsearchQueryRequest(request = queryRequest)
        // When
        val updatedQuery = sut.updateQueryWithParameters(dbResponse, queryParams)
        // Then
        updatedQuery shouldBe "color: green"
    }

    test("updateQueryWithParameters should use default value for combo type if not provided") {
        // Given
        val dynamicParameters = listOf(
            DynamicParameter(name = "COLOR", type = "combo", values = listOf("red", "green", "blue")),
        )
        val queryParams = QueryParams(parameters = mapOf())
        val queryRequest = QueryRequest(
            name = "testQuery",
            connectionName = "testConnection",
            index = "testIndex",
            query = "color: \$COLOR",
            dynamicParameters = dynamicParameters,
        )
        val dbResponse = ElasticsearchQueryRequest(request = queryRequest)
        // When
        val updatedQuery = sut.updateQueryWithParameters(dbResponse, queryParams)
        // Then
        updatedQuery shouldBe "color: red"
    }
})
