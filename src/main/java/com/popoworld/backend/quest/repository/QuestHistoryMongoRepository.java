package com.popoworld.backend.quest.repository;

import com.popoworld.backend.quest.entity.QuestHistory;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface QuestHistoryMongoRepository extends MongoRepository<QuestHistory, UUID> {

}
