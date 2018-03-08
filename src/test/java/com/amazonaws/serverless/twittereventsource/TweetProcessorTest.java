package com.amazonaws.serverless.twittereventsource;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TweetProcessorTest {
    private static final String TWEET_PROCESSOR_FUNCTION_NAME = "I will process ALL of the tweets!!";
    private static final int BATCH_SIZE = 2;

    @Mock
    private AWSLambda lambda;

    private TweetProcessor processor;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        processor = new TweetProcessor(TWEET_PROCESSOR_FUNCTION_NAME, BATCH_SIZE, lambda);
    }

    @Test
    public void accept_singleBatch() throws Exception {
        processor.accept(Lists.newArrayList("1", "2"));

        InvokeRequest expected = new InvokeRequest()
                .withFunctionName(TWEET_PROCESSOR_FUNCTION_NAME)
                .withInvocationType(InvocationType.Event)
                .withPayload("[\"1\",\"2\"]");
        verify(lambda).invoke(expected);

        verifyNoMoreInteractions(lambda);
    }

    @Test
    public void accept_multipleBatches() throws Exception {
        processor.accept(Lists.newArrayList("1", "2", "3", "4", "5"));

        InvokeRequest expected = new InvokeRequest()
                .withFunctionName(TWEET_PROCESSOR_FUNCTION_NAME)
                .withInvocationType(InvocationType.Event);

        verify(lambda).invoke(expected.withPayload("[\"1\",\"2\"]"));
        verify(lambda).invoke(expected.withPayload("[\"3\",\"4\"]"));
        verify(lambda).invoke(expected.withPayload("[\"5\"]"));
        verifyNoMoreInteractions(lambda);
    }
}