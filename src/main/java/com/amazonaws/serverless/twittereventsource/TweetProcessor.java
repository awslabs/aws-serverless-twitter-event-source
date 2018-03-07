package com.amazonaws.serverless.twittereventsource;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;

import com.google.common.collect.Lists;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import twitter4j.JSONArray;
import twitter4j.Status;
import twitter4j.TwitterObjectFactory;

/**
 * Proxies tweets to configured lambda function for processing.
 */
@RequiredArgsConstructor
@Slf4j
public class TweetProcessor implements Consumer<List<Status>> {
    @NonNull
    private final String tweetProcessorFunctionName;
    private final int batchSize;

    @NonNull
    private final AWSLambda lambda;

    /**
     * Splits the tweets up into batches based on configured batch size and proxies them to the configured lambda function for processing. Invokes the lambdas
     * using async invoke.
     *
     * @param tweets tweets to process.
     */
    @Override
    public void accept(List<Status> tweets) {
        Lists.partition(tweets, batchSize)
                .forEach(this::processBatch);
    }

    private void processBatch(List<Status> tweets) {
        String requestPayload = new JSONArray(tweets.stream()
                .map(TwitterObjectFactory::getRawJSON)
                .collect(Collectors.toList()))
                .toString();

        log.info("Invoking tweet processor lambda {} with request payload: {}", tweetProcessorFunctionName, requestPayload);

        InvokeRequest invokeRequest = new InvokeRequest()
                .withFunctionName(tweetProcessorFunctionName)
                .withInvocationType(InvocationType.Event)
                .withPayload(requestPayload);

        lambda.invoke(invokeRequest);
    }
}
