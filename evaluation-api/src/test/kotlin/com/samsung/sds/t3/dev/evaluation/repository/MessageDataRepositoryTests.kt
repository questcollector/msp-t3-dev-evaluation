package com.samsung.sds.t3.dev.evaluation.repository

import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.util.*


private const val TEST = "test"

@ExperimentalCoroutinesApi
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

    private val NOW = LocalDateTime.now().withNano(0)
    private val TODAY = LocalDateTime.parse("2023-02-22T23:57:06.578")
    private val YESTERDAY = TODAY.minusDays(1)
    private val SAMPLE_UUID = UUID.randomUUID()
    private val entities : MutableList<MessageDataEntity> =
        mutableListOf()

    @BeforeAll
    fun `테스트 데이터 입력`() {
        runTest {
            entities.add(messageDataRepository.save(MessageDataEntity(sentDateTime = TODAY)))
            entities.add(messageDataRepository.save(MessageDataEntity(sentDateTime = YESTERDAY)))
            entities.add(messageDataRepository.save(MessageDataEntity(sentDateTime = NOW, slackUserName = TEST)))
            entities.add(messageDataRepository.save(MessageDataEntity(sentDateTime = NOW, slackUserName = TEST)))
            entities.add(messageDataRepository.save(MessageDataEntity(id = SAMPLE_UUID, sentDateTime = NOW)))
        }
    }

    @AfterAll
    fun `테스트 데이터 삭제`() {
        runTest {
            messageDataRepository.deleteAll()
        }
    }

    @Test
    fun `유저 이름으로 보낸 메시지 조회하기`() {
        runTest {
            val result = messageDataRepository.findAllBySlackUserNameStartsWith(TEST)
            assertThat(result.toList()).containsAll(
                entities.subList(2, 4)
            )
        }
    }

    @Test
    fun `특정 uuid 데이터 조회`() {
        runTest {
            val result = messageDataRepository.findById(SAMPLE_UUID)
            assertThat(result).isEqualTo(entities[4])
        }
    }
}