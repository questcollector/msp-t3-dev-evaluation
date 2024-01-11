package com.github.questcollector.evaluation.repository

import com.github.questcollector.evaluation.repository.entity.MessageDataEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface MessageDataRepository: CoroutineCrudRepository<MessageDataEntity, UUID>