package com.example.infrastructure.boilerplate.elasticsearch

import org.apache.http.HttpHost
import org.elasticsearch.client.RestClient

class RestClientFactory {
    fun createRestClientWrapper(hosts: List<String>, port: Int): IRestClientWrapper {
        val httpHosts = hosts.map { HttpHost(it, port, "http") }.toTypedArray()
        val restClient = RestClient.builder(*httpHosts).build()
        return RestClientWrapper(restClient)
    }
}
