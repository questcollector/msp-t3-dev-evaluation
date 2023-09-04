package com.samsung.sds.t3.dev.evaluation.repository

import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepositoryTests.Constant.TODAY
import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepositoryTests.Constant.YESTERDAY
import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime


private const val TEST = "test"


@DataMongoTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class MessageDataRepositoryTests (
    @Autowired
    private val messageDataRepository: MessageDataRepository
) {

    private object Constant {
        val TODAY: LocalDateTime = LocalDateTime.parse("2023-02-22T23:57:06.578")
        val YESTERDAY: LocalDateTime = TODAY.minusDays(1)
    }
    private val entities : MutableList<MessageDataEntity> =
        mutableListOf()

    @BeforeAll
    fun `테스트 데이터 입력`() {
        runBlocking {
            entities.add(messageDataRepository.save(MessageDataEntity(sentDateTime = TODAY)))
            entities.add(messageDataRepository.save(MessageDataEntity(sentDateTime = YESTERDAY)))
            entities.add(messageDataRepository.save(MessageDataEntity(slackUserName = TEST)))
            entities.add(messageDataRepository.save(MessageDataEntity(slackUserName = TEST)))
            entities.add(messageDataRepository.save(MessageDataEntity()))
        }
    }


    @AfterAll
    fun `테스트 데이터 삭제`() {
        runBlocking {
            messageDataRepository.deleteAll()
        }
    }

    @Test
    fun `save 메소드 테스트`() { }

}