package com.amazonaws.serverless.twittereventsource;

import java.util.Date;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedTimestamp;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBVersionAttribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

/**
 * DDB record for storing the checkpoint.
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Wither
@DynamoDBTable(tableName = "")
public class CheckpointRecord {
    @DynamoDBHashKey
    private String id;

    @DynamoDBVersionAttribute
    private Long recordVersionNumber;

    @DynamoDBTypeConvertedTimestamp(pattern = "yyyy-MM-dd\'T\'HH:mm:ss.SSS\'Z\'")
    private Date checkpoint;
}
