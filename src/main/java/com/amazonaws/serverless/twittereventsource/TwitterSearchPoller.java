package com.amazonaws.serverless.twittereventsource;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Provides polling method to search for new tweets since last cutoff checkpoint.
 */
@RequiredArgsConstructor
@Slf4j
public class TwitterSearchPoller {
    @NonNull
    private final String queryText;
    @NonNull
    private final Twitter twitter;
    @NonNull
    private final SearchCheckpoint checkpoint;
    @NonNull
    private final TweetProcessor tweetProcessor;

    /**
     * Searches for new tweets since last poll (calculated by subtracting current time from pollingInterval). Calls tweetProcessor to
     * handle any tweets found.
     */
    public void poll() {
        Instant cutoff = checkpoint.get();
        List<Status> tweets = findTweetsSince(cutoff);

        if (tweets.isEmpty()) {
            log.info("No tweets found. Nothing to do.");
            return;
        }

        tweetProcessor.accept(tweets);

        Date mostRecent = tweets.stream()
                .map(Status::getCreatedAt)
                .max(Date::compareTo)
                .get();
        checkpoint.update(mostRecent.toInstant());
    }

    @SneakyThrows(TwitterException.class)
    private List<Status> findTweetsSince(Instant cutoff) {
        Query query = new Query(queryText);
        query.setCount(100);
        QueryResult result;
        List<Status> tweets = new ArrayList<>();
        do {
            log.info("Calling Twitter search API with query: {}", query);
            result = twitter.search(query);
            log.info("{} search results found.", result.getTweets().size());

            List<Status> tweetsWithinCutoff = result.getTweets().stream()
                    .filter(t -> t.getCreatedAt().toInstant().isAfter(cutoff))
                    .collect(Collectors.toList());

            log.info("{}/{} search results found within polling interval cutoff of {}.", tweetsWithinCutoff.size(), result.getTweets().size(), cutoff);
            tweets.addAll(tweetsWithinCutoff);

            if (tweetsWithinCutoff.size() < result.getTweets().size()) {
                log.info("Cutoff reached. Breaking out of search loop");
                break;
            }
        } while ((query = result.nextQuery()) != null);

        return tweets;
    }
}
