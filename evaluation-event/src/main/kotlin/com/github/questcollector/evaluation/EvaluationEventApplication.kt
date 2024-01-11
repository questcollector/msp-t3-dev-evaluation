package com.github.questcollector.evaluation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EvaluationEventApplication

fun main(args: Array<String>) {
	runApplication<EvaluationEventApplication>(*args)
}
