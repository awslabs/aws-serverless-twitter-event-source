package com.amazonaws.serverless.twittereventsource;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Maintains checkpoint of last tweet timestamp visited.
 */
@RequiredArgsConstructor
@Slf4j
public class SearchCheckpoint {
    private static final String RECORD_KEY = "checkpoint";

    private final DynamoDBMapper mapper;
    private final DynamoDBMapperConfig mapperConfig;

    public SearchCheckpoint(@NonNull final DynamoDBMapper mapper, @NonNull final String tableName) {
        this.mapper = mapper;
        this.mapperConfig = new DynamoDBMapperConfig.Builder()
                .withTableNameOverride(new DynamoDBMapperConfig.TableNameOverride(tableName))
                .build();
    }

    public Instant get() {
        return getOrDefault().getCheckpoint().toInstant();
    }

    public void update(@NonNull final Instant newCheckpoint) {
        CheckpointRecord record = getOrDefault();
        if (newCheckpoint.equals(record.getCheckpoint().toInstant())) {
            log.info("newCheckpoint {} is the same as the current checkpoint {}. Not updating.", newCheckpoint, record.getCheckpoint().toInstant());
            return;
        }

        log.info("Updating search checkpoint from {} to {}", record.getCheckpoint().toInstant(), newCheckpoint);
        mapper.save(record.withCheckpoint(Date.from(newCheckpoint)), mapperConfig);
    }

    private CheckpointRecord getOrDefault() {
        CheckpointRecord loadPrototype = CheckpointRecord.builder()
                .id(RECORD_KEY)
                .build();
        return Optional.ofNullable(mapper.load(loadPrototype, mapperConfig))
                .orElse(CheckpointRecord.builder()
                        .id(RECORD_KEY)
                        .checkpoint(Date.from(Instant.EPOCH))
                        .build());
    }
}
