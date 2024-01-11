package com.github.questcollector.evaluation.repository

import com.github.questcollector.evaluation.repository.MessageDataRepositoryTests.Constant.TODAY
import com.github.questcollector.evaluation.repository.MessageDataRepositoryTests.Constant.YESTERDAY
import com.github.questcollector.evaluation.repository.entity.MessageDataEntity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import java.time.LocalDateTime


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
        val TODAY: LocalDateTime = LocalDateTime.parse("2023-02-22T23:57:06.578")
        val YESTERDAY: LocalDateTime = TODAY.minusDays(1)
    }
    private val entities : MutableList<MessageDataEntity> =
        mutableListOf()

    @BeforeAll
    fun `테스트 데이터 입력`() {
        runTest {
            entities.add(messageDataRepository.save(MessageDataEntity(sentDateTime = TODAY)))
            entities.add(messageDataRepository.save(MessageDataEntity(sentDateTime = YESTERDAY)))
            entities.add(messageDataRepository.save(MessageDataEntity(slackUserName = TEST)))
            entities.add(messageDataRepository.save(MessageDataEntity(slackUserName = TEST)))
            entities.add(messageDataRepository.save(MessageDataEntity()))
        }
    }


    @AfterAll
    fun `테스트 데이터 삭제`() {
        runTest {
            messageDataRepository.deleteAll()
        }
    }

    @Test
    fun `save 메소드 테스트`() { }

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