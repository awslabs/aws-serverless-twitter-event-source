package com.amazonaws.serverless.twittereventsource.dagger;


import javax.inject.Singleton;

import com.amazonaws.serverless.twittereventsource.SearchCheckpoint;
import com.amazonaws.serverless.twittereventsource.TweetProcessor;
import com.amazonaws.serverless.twittereventsource.TwitterSearchPoller;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;

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
                .setJSONStoreEnabled(true)
                .setOAuthConsumerKey(Env.getConsumerKey())
                .setOAuthConsumerSecret(Env.getConsumerSecret())
                .setOAuthAccessToken(Env.getAccessToken())
                .setOAuthAccessTokenSecret(Env.getAccessTokenSecret());
        TwitterFactory factory = new TwitterFactory(cb.build());
        return new TwitterSearchPoller(Env.getSearchText(), factory.getInstance(), searchCheckpoint, tweetProcessor, Env.isStreamModeEnabled());
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
