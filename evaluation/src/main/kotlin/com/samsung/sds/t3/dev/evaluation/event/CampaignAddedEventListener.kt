package com.samsung.sds.t3.dev.evaluation.event

import com.samsung.sds.t3.dev.evaluation.model.CampaignDTO
import com.samsung.sds.t3.dev.evaluation.repository.entity.toMessageDataDTO
import com.samsung.sds.t3.dev.evaluation.service.MessageDataCommandService
import com.samsung.sds.t3.dev.evaluation.service.SlackMessagingService
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.messaging.Message
import org.springframework.messaging.support.ErrorMessage
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.function.Consumer

@Component
class CampaignAddedEventListener (
    private val messageDataCommandService: MessageDataCommandService,
    private val slackMessagingService: SlackMessagingService,
    private val notificationEventPublisher: NotificationEventPublisher
) {

    private val log: Logger = LoggerFactory.getLogger(this.javaClass)

    @Bean
    fun campaignAddedEvent(): Consumer<Message<CampaignDTO>> = Consumer { message ->

        runBlocking {
            val entity = messageDataCommandService.createMessageDataEntity(message)
            val saved = messageDataCommandService.saveMessageDataEntity(entity)
            log.info("Saved message:")
            if (log.isDebugEnabled) log.debug("$saved")

            val dto = saved.toMessageDataDTO()
            if (dto.isPass) {
                notificationEventPublisher.publishNotificationSuccessEvent(dto)
                slackMessagingService.postMessage(dto)
            } else {
                notificationEventPublisher.publishNotificationFailedEvent(dto.hostname!!)
            }
        }
    }

    @Bean
    fun errorHandler() : Consumer<Mono<ErrorMessage>> = Consumer { message ->
        message.log()
            .subscribe {
                log.info("error occured: \n$it")
            }
    }
}
