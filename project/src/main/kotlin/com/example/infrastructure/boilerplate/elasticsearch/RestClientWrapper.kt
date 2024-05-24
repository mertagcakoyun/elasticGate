package com.example.infrastructure.boilerplate.elasticsearch

import org.elasticsearch.client.Request
import org.elasticsearch.client.ResponseListener
import org.elasticsearch.client.RestClient

class RestClientWrapper(private val restClient: RestClient) : IRestClientWrapper {
    override fun performRequestAsync(request: Request, responseListener: ResponseListener) {
        restClient.performRequestAsync(request, responseListener)
    }

    override fun closeClient() {
        restClient.close()
    }
}
