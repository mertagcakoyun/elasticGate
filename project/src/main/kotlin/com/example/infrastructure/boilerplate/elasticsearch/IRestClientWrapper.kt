package com.example.infrastructure.boilerplate.elasticsearch

import org.apache.http.HttpHost
import org.elasticsearch.client.Request
import org.elasticsearch.client.ResponseListener
import org.elasticsearch.client.RestClient

interface IRestClientWrapper {
    fun performRequestAsync(request: Request, responseListener: ResponseListener)
    fun closeClient()
}
