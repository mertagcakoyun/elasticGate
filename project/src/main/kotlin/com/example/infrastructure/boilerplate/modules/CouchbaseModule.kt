package com.example.infrastructure.boilerplate.modules

import com.couchbase.client.core.cnc.DefaultEventBus
import com.couchbase.client.java.Cluster
import com.couchbase.client.java.ClusterOptions
import com.couchbase.client.java.ReactiveCluster
import com.couchbase.client.java.codec.JacksonJsonSerializer
import com.couchbase.client.java.env.ClusterEnvironment
import com.couchbase.client.java.json.JsonValueModule
import com.example.infrastructure.boilerplate.config.get
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.server.application.*
import org.koin.dsl.module
import reactor.core.scheduler.Schedulers
import java.time.Duration
import kotlin.math.max

fun Application.couchbaseModule(): org.koin.core.module.Module {
    return module {
        single {
            val connectionString = environment.config.get("couchbase.connection.connectionString").getString()
            val username = environment.config.get("couchbase.connection.username").getString()
            val password = environment.config.get("couchbase.connection.password").getString()

            val objectMapper = ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModules(KotlinModule.Builder().build(), JsonValueModule())
            val schedulerPoolSize = max(Schedulers.DEFAULT_POOL_SIZE, 8)
            val cbScheduler = Schedulers.newParallel("cb-comp", schedulerPoolSize, true)
            val cbEventBus = DefaultEventBus.create(cbScheduler)
            cbEventBus.start().block()
            val env = ClusterEnvironment
                .builder()
                .scheduler(cbScheduler)
                .eventBus(cbEventBus)
                .jsonSerializer(JacksonJsonSerializer.create(objectMapper))
                .build()
            val clusterOptions = ClusterOptions.clusterOptions(username, password)
                .environment(env)

            val cluster = Cluster.connect(connectionString, clusterOptions)
            cluster.waitUntilReady(Duration.ofMinutes(1))
            cluster.reactive()
        }

        single {
            val bucketName = System.getenv()["CB_BUCKET"] ?: "Exposer"
            get<ReactiveCluster>().bucket(bucketName).also {
                it.waitUntilReady(Duration.ofSeconds(30))
            }.defaultCollection()
        }
    }
}
