package com.samsung.sds.t3.dev.evaluation.controller

import com.samsung.sds.t3.dev.evaluation.config.MessageDataRouter
import com.samsung.sds.t3.dev.evaluation.model.MessageDataDTO
import com.samsung.sds.t3.dev.evaluation.service.MessageDataQueryService
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBodyList
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*

@WebFluxTest(MessageDataHandler::class)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@ContextConfiguration(classes = [MessageDataHandler::class, MessageDataRouter::class])
class MessageDataControllerTests {
    @MockBean
    private lateinit var messageDataQueryService: MessageDataQueryService
    @Autowired
    private lateinit var wtc: WebTestClient

    private val TODAY = OffsetDateTime.parse("2023-02-23T18:15:17+09:00")
    private val YESTERDAY = TODAY.minusDays(1)
    @Test
    @FlowPreview
    fun `uuid로 1건 조회하기`() {
        val messageDataDTO = MessageDataDTO(UUID.randomUUID().toString())
        runBlocking {
            given(messageDataQueryService.getMessageDataByMessageUuid(messageDataDTO.messageId!!))
                .willReturn(messageDataDTO)
        }

        wtc.get().uri("/api/messageData/uuid/${messageDataDTO.messageId!!}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .consumeWith { println(it) }
            .jsonPath("$.messageId").isEqualTo(messageDataDTO.messageId!!)
    }

    @Test
    fun `특정 시간대 데이터 조회하기`() {
        val startDateQueryParam = "2023-02-22T18:15:17"
        val endDateQueryParam = "2023-02-23T18:15:17"
        val startDate = LocalDateTime.parse(startDateQueryParam)
        val endDate = LocalDateTime.parse(endDateQueryParam)

        val messageDTOList = listOf<MessageDataDTO>(
            MessageDataDTO(sentDateTime = TODAY.minusHours(1)),
            MessageDataDTO(sentDateTime = YESTERDAY))
        messageDTOList.forEach { println(it) }

        runBlocking {
            given(messageDataQueryService.getMessageDataDuring(startDate, endDate))
                .willReturn(messageDTOList.asFlow())
        }

        wtc.get().uri {
            it.path("/api/messageData/")
                .queryParam("startDate", startDateQueryParam)
                .queryParam("endDate", endDateQueryParam)
                .build()
        }
            .exchange()
            .expectStatus().isOk
            .expectBodyList<MessageDataDTO>()
            .contains(
                messageDTOList[0], messageDTOList[1]
            )
    }

    @Test
    fun `이상한 시간 데이터 넣어보기`() {
        val startDateQueryParam = "something-wrong-data"
        val endDateQueryParam = "something-wrong-data"

        wtc.get().uri {
            it.path("/api/messageData/")
                .queryParam("startDate", startDateQueryParam)
                .queryParam("endDate", endDateQueryParam)
                .build()
        }
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `특정 유저의 데이터 조회`() {
        val messageDTOList = listOf<MessageDataDTO>(
            MessageDataDTO(slackUserName = "test"),
            MessageDataDTO(slackUserName = "test"))
        messageDTOList.forEach { println(it) }

        runBlocking {
            given(messageDataQueryService.getMessageDataWithSlackUserName("test"))
                .willReturn(messageDTOList.asFlow())
        }

        wtc.get().uri ("/api/messageData/slackUserName/test")
            .exchange()
            .expectStatus().isOk
            .expectBodyList<MessageDataDTO>()
            .contains(
                messageDTOList[0], messageDTOList[1]
            )
    }
}