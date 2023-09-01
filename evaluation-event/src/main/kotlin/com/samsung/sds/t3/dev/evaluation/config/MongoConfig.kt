package com.samsung.sds.t3.dev.evaluation.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories


@Profile("prod")
@Configuration
@EnableReactiveMongoRepositories(basePackages = ["com.samsung.sds.t3.dev.evaluation.repository"])
class MongoConfig