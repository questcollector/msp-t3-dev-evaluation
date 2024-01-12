package com.github.questcollector.evaluation.event.validator

import com.github.questcollector.evaluation.model.SampleDTO
import org.springframework.cloud.stream.converter.ConversionException
import org.springframework.messaging.Message
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component

@Component
class SampleDTOMessageValidator {
    fun validate(message: Message<out SampleDTO>) : Message<SampleDTO> {
        val payload = message.payload

        when {
            payload.id <= 0 -> {
                throw ConversionException("payload id is zero or below zero")
            }
            payload.name.isBlank() -> {
                throw ConversionException("payload name length is below zero")
            }
            else -> return MessageBuilder.createMessage(message.payload, message.headers)
        }
    }
}