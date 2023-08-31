package com.samsung.sds.t3.dev.evaluation.event

import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.cloud.stream.binder.test.OutputDestination
import org.springframework.test.context.ActiveProfiles

@ExperimentalCoroutinesApi
@ActiveProfiles("test")
@SpringBootTest
@EnableAutoConfiguration(exclude= [EmbeddedMongoAutoConfiguration::class])
@ExtendWith(MockitoExtension::class)
class NotificationEventBinderTests {

    @Autowired
    lateinit var outputDestination: OutputDestination

    @SpyBean
    lateinit var notificationEventPublisher: NotificationEventPublisher

    @Test
    fun `success event 테스트`() = runTest {
        val messageDataDTO = MessageDataEntity(slackUserId = "id", slackUserName = "test", isPass = true)
        notificationEventPublisher.publishNotificationSuccessEvent(messageDataDTO)

        val outputBinding = "notificationSuccessEvent"
        val output = outputDestination.receive(0, outputBinding)
        val payload = """
            Excellent @${messageDataDTO.slackUserName}, 
            You have successfully completed the development practice assignment
            You can check the same UUID below in the DM on Slack
            ==========================================
            ${messageDataDTO.id}
            ==========================================
            
        """.trimIndent()

        assertThat(output.headers["routingkey"] as String).isEqualTo("id")
        assertThat(output.payload).isEqualTo(payload.toByteArray())
    }

    @Test
    fun `failed event 테스트`() = runTest {
        val messageDataDTO = MessageDataEntity(slackUserId = "id", instanceId = "instance-id", isPass = true)
        notificationEventPublisher.publishNotificationFailedEvent(messageDataDTO)

        val outputBinding = "notificationFailedEvent"
        val output = outputDestination.receive(0, outputBinding)
        val payload = """
            This is reply to the message from EC2 instance which id is ${messageDataDTO.instanceId}.
            The requirement was not fulfilled. Please check the following information.
            1. Please check "slack.user.id" property in application.properties file.
            2. Please check implementation of "CampaignEventChannelInterceptor.preSend()" method.
            
        """.trimIndent()

        assertThat(output.headers["routingkey"] as String).isEqualTo("id")
        assertThat(output.payload).isEqualTo(payload.toByteArray())
    }
}