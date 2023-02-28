package com.samsung.sds.t3.dev.evaluation.repository

import com.samsung.sds.t3.dev.evaluation.repository.entity.MessageDataEntity
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import java.util.*


@Repository
interface MessageDataRepository: CoroutineCrudRepository<MessageDataEntity, UUID> {}