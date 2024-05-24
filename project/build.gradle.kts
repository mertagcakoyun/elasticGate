val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val koin_version: String by project
val exposed_version: String by project
val prometheus_version: String by project
val mockk_version: String by project
val apache_poi_version: String by project
val junit_jupiter_version: String by project
val jackson_version: String by project
val jul_to_slf4j_version: String by project
val stove_version: String by project

plugins {
    application
    kotlin("jvm") version "1.9.20"
    kotlin("plugin.serialization") version "1.9.20"
    idea
    id("com.github.johnrengelman.shadow") version "8.1.1" apply true
}

group = "com.example"
application {
    mainClass.set("com.example.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    // ktor
    implementation("io.insert-koin:koin-ktor:3.5.1")
    implementation("io.ktor:ktor-server:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-serialization:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jackson_version")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("io.ktor:ktor-server-resources:$ktor_version")

    // ktor-client
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-content-negotiation:$ktor_version")
    implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.0")
    implementation("io.dropwizard.metrics:metrics-jmx:4.0.0")
    implementation("com.jtransc:jtransc-rt:0.3.1")

    // data
    implementation("com.couchbase.client:java-client:3.4.11")
    implementation("co.elastic.clients:elasticsearch-java:7.17.14")
    implementation("org.elasticsearch.client:elasticsearch-rest-high-level-client:7.17.14")
    implementation("org.elasticsearch:elasticsearch:7.17.14")
    implementation("io.github.config4k:config4k:0.4.2")

    // kediatR
    implementation("com.trendyol:kediatr-core:3.0.0")
    implementation("com.trendyol:kediatr-koin-starter:3.0.0")

    // logging
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.slf4j:jul-to-slf4j:$jul_to_slf4j_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.7.3")

    // swagger
    implementation("io.ktor:ktor-server-swagger-jvm:$ktor_version")
    implementation("io.swagger.parser.v3:swagger-parser-v3:2.1.15")
    implementation("io.swagger.core.v3:swagger-core:2.2.15")
    implementation("io.ktor:ktor-server-html-builder-jvm:$ktor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:0.9.1")

    // Kotest
    testImplementation("io.kotest:kotest-runner-junit5:5.3.2")
    testImplementation("io.kotest:kotest-assertions-core:5.3.2")
    testImplementation("io.kotest:kotest-property:5.3.2")
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.mockito:mockito-inline:5.1.0")
    testImplementation("io.mockk:mockk:1.12.2")
    testImplementation("com.trendyol:stove-testing-e2e:$stove_version")
    testImplementation("com.trendyol:stove-testing-e2e-http:$stove_version")
    testImplementation("com.trendyol:stove-testing-e2e-elasticsearch:$stove_version")
    testImplementation("com.trendyol:stove-testing-e2e-couchbase:$stove_version")
}

sourceSets {
    create("test-e2e") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }

    val testE2eImplementation by configurations.getting {
        extendsFrom(configurations.testImplementation.get())
    }
    configurations["testE2eRuntimeOnly"].extendsFrom(configurations.runtimeOnly.get())
}

idea {
    module {
        testSources.from(project.sourceSets["test-e2e"].allSource.srcDirs)
        testResources.from(project.sourceSets["test-e2e"].resources.srcDirs)
    }
}

val e2eTest = task<Test>("e2eTest") {
    description = "Runs e2e tests."
    group = "verification"

    testClassesDirs = sourceSets["test-e2e"].output.classesDirs
    classpath = sourceSets["test-e2e"].runtimeClasspath

    useJUnitPlatform()
    reports {
        junitXml.required.set(true)
        html.required.set(true)
    }
}

val integrationTest = task<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
}
val codeCoverageReport = task<TestReport>("codeCoverageReport") {
    description = "Code coverage report started."
}
val testAggregateReports = tasks.register<TestReport>("testAggregateReports") {
    description = "test aggregate report started."
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    System.setProperty("scala.compat.version", "2.13")
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "19"
        allWarningsAsErrors = false
    }
}

tasks.withType<JavaCompile> {
    System.setProperty("scala.compat.version", "2.13")
    this.targetCompatibility = "19"
}

tasks.shadowJar {
    archiveBaseName.set(rootProject.name)
    archiveClassifier.set("")
    archiveVersion.set("")
    dependsOn(tasks.distTar, tasks.distZip)
}

tasks.test {
    useJUnitPlatform()
    reports {
        junitXml.required.set(true)
        html.required.set(true)
    }
}
