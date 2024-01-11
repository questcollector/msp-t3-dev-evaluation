package com.github.questcollector.evaluation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EvaluationApiApplication

fun main(args: Array<String>) {
	runApplication<EvaluationApiApplication>(*args)
}
