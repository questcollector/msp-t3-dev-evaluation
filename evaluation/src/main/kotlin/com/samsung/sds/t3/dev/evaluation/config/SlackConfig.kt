package com.samsung.sds.t3.dev.evaluation.config

import com.slack.api.Slack
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SlackConfig {
    @Bean
    fun getSlackInstance() : Slack = Slack.getInstance()
}