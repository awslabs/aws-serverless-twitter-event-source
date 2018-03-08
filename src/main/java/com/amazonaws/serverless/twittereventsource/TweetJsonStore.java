package com.amazonaws.serverless.twittereventsource;

import twitter4j.Status;
import twitter4j.TwitterObjectFactory;

/**
 * Simple wrapper around static twitter4j.TwitterObjectFactory for better testability of other classes.
 */
public class TweetJsonStore {
    /**
     * Returns raw json for the given tweet. Note, this relies on the static twitter4j.TwitterObjectFactory class which stores its tweet JSON in a threadlocal that gets cleared
     * on each new request to the Twitter API. So this will only return raw json for the tweets returned in the last request.
     * <p>
     * Also, in order for this to work, you have to enable json store when configuring the Twitter client. See {@link twitter4j.conf.ConfigurationBuilder#setJSONStoreEnabled(boolean)}.
     *
     * @param tweet tweet to get json for.
     * @return raw json for given tweet (if found).
     */
    public String getRawJson(Status tweet) {
        return TwitterObjectFactory.getRawJSON(tweet);
    }
}
