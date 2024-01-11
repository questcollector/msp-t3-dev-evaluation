package com.samsung.sds.t3.dev.evaluation.repository

import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepositoryTests.Constant.NOW
import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepositoryTests.Constant.SAMPLE_UUID
import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepositoryTests.Constant.TODAY
import com.samsung.sds.t3.dev.evaluation.repository.MessageDataRepositoryTests.Constant.YESTERDAY
import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactor.asFlux
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import reactor.test.StepVerifier
import java.time.LocalDateTime
import java.util.*


private const val TEST = "test"


@ExperimentalCoroutinesApi
@DataMongoTest
@ContextConfiguration(initializers = [MongoDBContainerInitializer::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@DisabledIfEnvironmentVariable(named = "GITHUB_ACTIONS", matches = "true")
class MessageDataRepositoryTests (
    @Autowired
    private val messageDataRepository: MessageDataRepository
) {

    private object Constant {
        val NOW: LocalDateTime = LocalDateTime.now().withNano(0)
        val TODAY: LocalDateTime = LocalDateTime.parse("2023-02-22T23:57:06.578")
        val YESTERDAY: LocalDateTime = TODAY.minusDays(1)
        val SAMPLE_UUID: UUID = UUID.randomUUID()
    }
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
            entities.add(messageDataRepository.save(MessageDataEntity(instanceId = TEST)))
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
        val result = messageDataRepository.findAllBySlackUserNameStartsWith(TEST)
        StepVerifier.create(result.asFlux())
            .expectNext (entities[2], entities[3])
            .expectComplete()
            .log()
            .verify()
    }

    @Test
    fun `특정 uuid 데이터 조회`() {
        runTest {
            val result = messageDataRepository.findById(SAMPLE_UUID)
            assertThat(result).isEqualTo(entities[4])
        }
    }

    @Test
    fun `특정 instanceId의 데이터 조회`() {
        val result = messageDataRepository.findAllByInstanceId(TEST)
        StepVerifier.create(result.asFlux())
            .expectNext(entities[5])
            .verifyComplete()
    }
}
class MongoDBContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    private val mongoDbContainer = MongoDBContainer(DockerImageName.parse("mongo:6.0.4"))
    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        mongoDbContainer.start()
        TestPropertyValues.of(
            "spring.data.mongodb.host=" + mongoDbContainer.host,
            "spring.data.mongodb.port=" + mongoDbContainer.getMappedPort(27017)
        ).applyTo(applicationContext);
    }

}