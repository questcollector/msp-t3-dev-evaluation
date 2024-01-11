package com.github.questcollector.evaluation.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories


@Profile("prod")
@Configuration
@EnableReactiveMongoRepositories(basePackages = ["com.github.questcollector.evaluation.repository"])
class MongoConfig