package com.example.infrastructure.boilerplate.modules

import com.example.application.connection.service.ConnectionService
import com.example.application.elasticQuery.service.ElasticsearchQueryService
import com.example.application.elasticQuery.service.QueryExecutionService
import com.example.application.elasticQuery.service.QueryStorageService
import com.example.infrastructure.boilerplate.elasticsearch.RestClientFactory
import org.koin.dsl.module

val mainModule = module {
    single { ElasticsearchQueryService() }
    single { RestClientFactory() }
    single { ConnectionService(get(), get()) }
    single { QueryStorageService(get(), get(), get()) }
    single { QueryExecutionService(get(), get(), get(), get()) }
}
