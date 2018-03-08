package com.amazonaws.serverless.twittereventsource;

import java.util.List;
import java.util.function.Consumer;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;

import com.google.common.collect.Lists;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import twitter4j.JSONArray;

/**
 * Proxies tweets to configured lambda function for processing.
 */
@RequiredArgsConstructor
@Slf4j
public class TweetProcessor implements Consumer<List<String>> {
    @NonNull
    private final String tweetProcessorFunctionName;
    private final int batchSize;

    @NonNull
    private final AWSLambda lambda;

    /**
     * Splits the tweets up into batches based on configured batch size and proxies them to the configured lambda function for processing. Invokes the lambdas
     * using async invoke.
     *
     * @param tweetsAsJson tweets to process in their raw JSON format.
     */
    @Override
    public void accept(List<String> tweetsAsJson) {
        Lists.partition(tweetsAsJson, batchSize)
                .forEach(this::processBatch);
    }

    private void processBatch(List<String> tweetsAsJson) {
        String requestPayload = new JSONArray(tweetsAsJson).toString();

        log.info("Invoking tweet processor lambda {} to process {} tweets", tweetProcessorFunctionName, tweetsAsJson.size());

        InvokeRequest invokeRequest = new InvokeRequest()
                .withFunctionName(tweetProcessorFunctionName)
                .withInvocationType(InvocationType.Event)
                .withPayload(requestPayload);

        lambda.invoke(invokeRequest);
    }
}
