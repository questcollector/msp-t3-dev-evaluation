package com.samsung.sds.t3.dev.evaluation.config

import io.swagger.v3.oas.models.info.Info
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun messageDataOpenApi(@Value("\${springdoc.version:v1}") appVersion: String? = "v1"): GroupedOpenApi? {
        val messageDataPaths = "/api/messageData/**"
        val evaluationDataPaths = "/api/evaluation/**"
        return GroupedOpenApi.builder().group("messageData")
            .addOpenApiCustomizer { openApi -> openApi.info(Info().title("messageData API").version(appVersion)) }
            .pathsToMatch(messageDataPaths, evaluationDataPaths)
            .build()
    }
}