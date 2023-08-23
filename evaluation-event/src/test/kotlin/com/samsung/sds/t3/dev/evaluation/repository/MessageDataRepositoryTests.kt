package com.samsung.sds.t3.dev.evaluation.repository

import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime


private const val TEST = "test"

@DataMongoTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@Import(EmbeddedMongoAutoConfiguration::class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class MessageDataRepositoryTests (
    @Autowired
    private val messageDataRepository: MessageDataRepository
) {

    private val TODAY = LocalDateTime.parse("2023-02-22T23:57:06.578")
    private val YESTERDAY = TODAY.minusDays(1)
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

}