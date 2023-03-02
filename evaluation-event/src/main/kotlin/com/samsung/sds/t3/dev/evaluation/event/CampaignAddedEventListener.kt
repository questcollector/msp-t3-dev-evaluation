package com.samsung.sds.t3.dev.evaluation.event

import com.samsung.sds.t3.dev.evaluation.model.CampaignDTO
import com.samsung.sds.t3.dev.evaluation.service.MessageDataCommandService
import com.samsung.sds.t3.dev.evaluation.service.SlackMessagingService
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
        message.concatMap {
            mono { handleMessage(it) }
        }.onErrorContinue { e, _ ->
            log.error(e.message, e)
        }.subscribe()
    }

    suspend fun handleMessage(message: Message<CampaignDTO>) {
        val entity = messageDataCommandService.createMessageDataEntity(message)
        val saved = messageDataCommandService.saveMessageDataEntity(entity)

        log.info("Saved message:")
        if (log.isDebugEnabled) log.debug("$saved")

        if (saved.isPass) {
            slackMessagingService.postMessage(saved)
            notificationEventPublisher.publishNotificationSuccessEvent(saved)
        } else {
            notificationEventPublisher.publishNotificationFailedEvent(saved.hostname!!)
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
