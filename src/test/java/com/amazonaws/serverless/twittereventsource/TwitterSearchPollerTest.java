package com.amazonaws.serverless.twittereventsource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.common.collect.Lists;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;

public class TwitterSearchPollerTest {
    private static final String QUERY_TEXT = "Find me some tweets!";
    private static final Instant CHECKPOINT = Instant.now();
    private static final Instant BEFORE_CHECKPOINT = CHECKPOINT.minusSeconds(1);
    private static final Instant AFTER_CHECKPOINT = CHECKPOINT.plusSeconds(1);

    @Mock
    private Twitter twitter;
    @Mock
    private SearchCheckpoint checkpoint;
    @Mock
    private TweetProcessor tweetProcessor;
    @Mock
    private TweetJsonStore tweetJsonStore;

    private TwitterSearchPoller poller;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(checkpoint.get()).thenReturn(CHECKPOINT);
    }

    @Test
    public void poll_streamModeDisabled_noSearchResults() throws Exception {
        poller = newPoller(false);

        setupSearchResults();
        poller.poll();

        verifyNoMoreInteractions(checkpoint, tweetProcessor);
    }

    @Test
    public void poll_streamModeDisabled_onePageSearchResults() throws Exception {
        poller = newPoller(false);

        setupSearchResults(mockTweet("1", BEFORE_CHECKPOINT), mockTweet("2", AFTER_CHECKPOINT));
        poller.poll();

        verify(tweetProcessor).accept(Lists.newArrayList("1", "2"));
        verifyNoMoreInteractions(checkpoint, tweetProcessor);
    }

    @Test
    public void poll_streamModeDisabled_multipageSearchResults() throws Exception {
        poller = newPoller(false);

        QueryResult firstResult = mock(QueryResult.class);
        List<Status> firstResultTweets = Lists.newArrayList(mockTweet("1", BEFORE_CHECKPOINT), mockTweet("2", AFTER_CHECKPOINT));
        when(firstResult.getTweets()).thenReturn(firstResultTweets);
        when(firstResult.nextQuery()).thenReturn(new Query(QUERY_TEXT));

        QueryResult secondResult = mock(QueryResult.class);
        List<Status> secondResultTweets = Lists.newArrayList(mockTweet("3", BEFORE_CHECKPOINT), mockTweet("4", AFTER_CHECKPOINT));
        when(secondResult.getTweets()).thenReturn(secondResultTweets);

        when(twitter.search(any(Query.class)))
                .thenReturn(firstResult)
                .thenReturn(secondResult);

        poller.poll();

        verify(tweetProcessor).accept(Lists.newArrayList("1", "2", "3", "4"));
        verifyNoMoreInteractions(checkpoint, tweetProcessor);
    }

    @Test
    public void poll_streamModeEnabled_noSearchResults() throws Exception {
        poller = newPoller(true);

        setupSearchResults();
        poller.poll();

        verify(checkpoint).get();
        verifyNoMoreInteractions(checkpoint, tweetProcessor);
    }

    @Test
    public void poll_streamModeEnabled_onePageSearchResults() throws Exception {
        poller = newPoller(true);

        setupSearchResults(mockTweet("1", BEFORE_CHECKPOINT), mockTweet("2", AFTER_CHECKPOINT));
        poller.poll();

        verify(checkpoint).get();
        verify(tweetProcessor).accept(Lists.newArrayList("2"));
        verify(checkpoint).update(AFTER_CHECKPOINT);
        verifyNoMoreInteractions(checkpoint, tweetProcessor);
    }

    @Test
    public void poll_streamModeEnabled_multipageSearchResults() throws Exception {
        poller = newPoller(true);

        QueryResult firstResult = mock(QueryResult.class);
        List<Status> firstResultTweets = Lists.newArrayList(mockTweet("1", AFTER_CHECKPOINT), mockTweet("2", AFTER_CHECKPOINT));
        when(firstResult.getTweets()).thenReturn(firstResultTweets);
        when(firstResult.nextQuery()).thenReturn(new Query(QUERY_TEXT));

        QueryResult secondResult = mock(QueryResult.class);
        List<Status> secondResultTweets = Lists.newArrayList(mockTweet("3", BEFORE_CHECKPOINT), mockTweet("4", BEFORE_CHECKPOINT));
        when(secondResult.getTweets()).thenReturn(secondResultTweets);

        when(twitter.search(any(Query.class)))
                .thenReturn(firstResult)
                .thenReturn(secondResult);

        poller.poll();

        verify(checkpoint).get();
        verify(tweetProcessor).accept(Lists.newArrayList("1", "2"));
        verify(checkpoint).update(AFTER_CHECKPOINT);
        verifyNoMoreInteractions(checkpoint, tweetProcessor);
    }

    private void setupSearchResults(Status... tweets) throws Exception {
        QueryResult result = mock(QueryResult.class);
        when(result.getTweets()).thenReturn(Arrays.asList(tweets));
        when(twitter.search(any(Query.class))).thenReturn(result);
    }

    private TwitterSearchPoller newPoller(boolean streamModeEnabled) {
        return new TwitterSearchPoller(QUERY_TEXT, twitter, checkpoint, tweetProcessor, tweetJsonStore, streamModeEnabled);
    }

    private Status mockTweet(String json, Instant createdAt) {
        Status tweet = mock(Status.class);
        when(tweet.getCreatedAt()).thenReturn(Date.from(createdAt));
        when(tweetJsonStore.getRawJson(tweet)).thenReturn(json);

        return tweet;
    }
}