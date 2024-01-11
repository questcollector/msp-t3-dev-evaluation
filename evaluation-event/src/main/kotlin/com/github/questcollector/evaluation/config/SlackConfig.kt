package com.github.questcollector.evaluation.config

import com.slack.api.Slack
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableCaching
class SlackConfig {
    @Bean
    fun getSlackInstance() : Slack = Slack.getInstance()
}