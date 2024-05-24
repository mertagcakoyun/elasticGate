package com.example.infrastructure.boilerplate.utils

private const val PROD_ENV_IDENTIFIER = "prod"
private const val STAGE_ENV_IDENTIFIER = "stage"
private const val DEFAULT_ENV = STAGE_ENV_IDENTIFIER

fun getEnvironment(): String {
    return System.getenv()["KTOR_ENV"] ?: DEFAULT_ENV
}

fun isProd(): Boolean = getEnvironment() == PROD_ENV_IDENTIFIER

fun isNotProd(): Boolean = !isProd()
