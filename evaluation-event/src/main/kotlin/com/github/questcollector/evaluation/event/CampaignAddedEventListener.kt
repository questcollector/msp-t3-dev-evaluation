package com.github.questcollector.evaluation.event

import com.github.questcollector.evaluation.event.validator.SampleDTOMessageValidator
import com.github.questcollector.evaluation.model.SampleDTO
import com.github.questcollector.evaluation.service.MessageDataCommandService
import com.github.questcollector.evaluation.service.SlackMessagingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.reactor.mono
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.messaging.Message
import org.springframework.messaging.support.ErrorMessage
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import java.util.function.Consumer

@Component
class CampaignAddedEventListener (
    private val messageDataCommandService: MessageDataCommandService,
    private val slackMessagingService: SlackMessagingService,
    private val notificationEventPublisher: NotificationEventPublisher,
    private val validator: SampleDTOMessageValidator
) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun campaignAddedEvent(): Consumer<Flux<Message<SampleDTO>>> = Consumer { message ->
        log.info("campaignAddedEvent invoked")
        message.doOnNext{
            validator.validate(it)
        }.concatMap {
            mono(Dispatchers.IO) { handleMessage(it) }
        }.onErrorContinue { e, _ ->
            log.error(e.message, e)
        }.subscribe()
    }

    private suspend fun handleMessage(message: Message<SampleDTO>) {

        val entity = messageDataCommandService.createMessageDataEntity(message)

        if (entity.isPass) {
            log.info("student send appropriate message")
            slackMessagingService.postMessage(entity)
            notificationEventPublisher.publishNotificationSuccessEvent(entity)
        } else {
            log.info("student send erroneous message")
            entity.slackUserId?.run {
                notificationEventPublisher.publishNotificationFailedEvent(entity)
            }
        }
    }

    @Bean
    fun errorHandler() : Consumer<Flux<ErrorMessage>> = Consumer { message ->
        message.log()
            .subscribe {
                log.info("error occurred: \n$it")
            }
    }
}
