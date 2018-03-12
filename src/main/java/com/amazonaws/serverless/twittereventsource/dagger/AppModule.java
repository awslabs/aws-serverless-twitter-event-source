package com.amazonaws.serverless.twittereventsource.dagger;


import java.nio.ByteBuffer;
import java.util.Base64;

import javax.inject.Singleton;

import com.amazonaws.serverless.twittereventsource.SearchCheckpoint;
import com.amazonaws.serverless.twittereventsource.TweetJsonStore;
import com.amazonaws.serverless.twittereventsource.TweetProcessor;
import com.amazonaws.serverless.twittereventsource.TwitterSearchPoller;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.kms.model.DecryptRequest;
import com.amazonaws.services.kms.model.DecryptResult;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.util.StringUtils;

import com.google.common.base.Charsets;

import dagger.Module;
import dagger.Provides;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Application DI wiring.
 */
@Module
public class AppModule {
    @Provides
    @Singleton
    public TwitterSearchPoller provideTwitterSearchPoller(final SearchCheckpoint searchCheckpoint, final TweetProcessor tweetProcessor) {
        ConfigurationBuilder cb = new ConfigurationBuilder()
                .setJSONStoreEnabled(true);
        populateApiKeys(cb);

        TwitterFactory factory = new TwitterFactory(cb.build());
        return new TwitterSearchPoller(Env.getSearchText(), factory.getInstance(), searchCheckpoint, tweetProcessor, new TweetJsonStore(), Env.isStreamModeEnabled());
    }

    private void populateApiKeys(final ConfigurationBuilder cb) {
        AWSKMS kms = AWSKMSClientBuilder.standard().build();
        cb.setOAuthConsumerKey(getEncryptedParameter(kms, Env.getEncryptedConsumerKey(), Env.getPlaintextConsumerKey()));
        cb.setOAuthConsumerSecret(getEncryptedParameter(kms, Env.getEncryptedConsumerSecret(), Env.getPlaintextConsumerSecret()));
        cb.setOAuthAccessToken(getEncryptedParameter(kms, Env.getEncryptedAccessToken(), Env.getPlaintextAccessToken()));
        cb.setOAuthAccessTokenSecret(getEncryptedParameter(kms, Env.getEncryptedAccessTokenSecret(), Env.getPlaintextAccessTokenSecret()));
    }

    private static String getEncryptedParameter(final AWSKMS kms, final String encryptedBase64, final String plaintextFallback) {
        if (StringUtils.isNullOrEmpty(encryptedBase64)) {
            return plaintextFallback;
        }
        return decrypt(kms, encryptedBase64);
    }

    private static String decrypt(AWSKMS kms, String ciphertextBlobBase64) {
        byte[] bytes = Base64.getDecoder().decode(ciphertextBlobBase64);
        DecryptResult result = kms.decrypt(new DecryptRequest()
                .withCiphertextBlob(ByteBuffer.wrap(bytes)));
        return new String(result.getPlaintext().array(), Charsets.UTF_8);
    }

    @Provides
    @Singleton
    public SearchCheckpoint provideSearchCheckpoint() {
        AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDBMapper mapper = new DynamoDBMapper(dynamoDB);
        return new SearchCheckpoint(mapper, Env.getSearchCheckpointTableName());
    }

    @Provides
    @Singleton
    public TweetProcessor provideTweetProcessor() {
        AWSLambda lambda = AWSLambdaClientBuilder.standard().build();
        return new TweetProcessor(Env.getTweetProcessorFunctionName(), Env.getBatchSize(), lambda);
    }
}
