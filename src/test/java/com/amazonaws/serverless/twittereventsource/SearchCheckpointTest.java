package com.amazonaws.serverless.twittereventsource;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.time.Instant;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SearchCheckpointTest {
    private static final String TABLE_NAME = "dinner";

    @Mock
    private DynamoDBMapper mapper;

    private SearchCheckpoint checkpoint;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        checkpoint = new SearchCheckpoint(mapper, TABLE_NAME);
    }

    @Test
    public void get_noRecordFound() throws Exception {
        assertThat(checkpoint.get(), is(Instant.EPOCH));

        ArgumentCaptor<DynamoDBMapperConfig> captor = ArgumentCaptor.forClass(DynamoDBMapperConfig.class);
        verify(mapper).load(eq(CheckpointRecord.builder().id(SearchCheckpoint.RECORD_KEY).build()), captor.capture());
        assertThat(captor.getValue().getTableNameOverride().getTableName(), is(TABLE_NAME));
    }

    @Test
    public void get_recordFound() throws Exception {
        Instant previousTimestamp = Instant.now();
        when(mapper.load(any(CheckpointRecord.class), any(DynamoDBMapperConfig.class))).thenReturn(CheckpointRecord.builder()
                .id(SearchCheckpoint.RECORD_KEY)
                .checkpoint(Date.from(previousTimestamp))
                .build());

        assertThat(checkpoint.get(), is(previousTimestamp));
    }

    @Test
    public void update_sameCheckpoint() throws Exception {
        checkpoint.update(Instant.EPOCH);
        verify(mapper, never()).save(any(), any(DynamoDBMapperConfig.class));
    }

    @Test
    public void update_newCheckpoint() throws Exception {
        Instant newTimestamp = Instant.now();
        checkpoint.update(newTimestamp);

        ArgumentCaptor<DynamoDBMapperConfig> captor = ArgumentCaptor.forClass(DynamoDBMapperConfig.class);
        CheckpointRecord expectedRecord = CheckpointRecord.builder()
                .id(SearchCheckpoint.RECORD_KEY)
                .checkpoint(Date.from(newTimestamp))
                .build();
        verify(mapper).save(eq(expectedRecord), captor.capture());
        assertThat(captor.getValue().getTableNameOverride().getTableName(), is(TABLE_NAME));
    }
}