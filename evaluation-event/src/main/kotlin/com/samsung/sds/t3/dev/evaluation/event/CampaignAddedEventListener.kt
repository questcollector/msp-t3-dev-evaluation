package com.samsung.sds.t3.dev.evaluation.event

import com.samsung.sds.t3.dev.evaluation.model.CampaignDTO
import com.samsung.sds.t3.dev.evaluation.service.MessageDataCommandService
import com.samsung.sds.t3.dev.evaluation.service.SlackMessagingService
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
    private val notificationEventPublisher: NotificationEventPublisher
) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun campaignAddedEvent(): Consumer<Flux<Message<CampaignDTO>>> = Consumer { message ->
        log.info("campaignAddedEvent invoked")
        message.concatMap {
            mono(Dispatchers.IO) { handleMessage(it) }
        }.onErrorContinue { e, _ ->
            log.error(e.message, e)
        }.subscribe()
    }

    suspend fun handleMessage(message: Message<CampaignDTO>) {

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
                log.info("error occured: \n$it")
            }
    }
}
