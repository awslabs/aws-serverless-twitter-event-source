package com.amazonaws.serverless.twittereventsource.dagger;

/**
 * Helper class for fetching environment values.
 */
public final class Env {
    public static final String CONSUMER_KEY_KEY = "CONSUMER_KEY";
    public static final String CONSUMER_SECRET_KEY = "CONSUMER_SECRET";
    public static final String ACCESS_TOKEN_KEY = "ACCESS_TOKEN";
    public static final String ACCESS_TOKEN_SECRET_KEY = "ACCESS_TOKEN_SECRET";
    public static final String SEARCH_TEXT_KEY = "SEARCH_TEXT";
    public static final String SEARCH_CHECKPOINT_TABLE_NAME_KEY = "SEARCH_CHECKPOINT_TABLE_NAME";
    public static final String TWEET_PROCESSOR_FUNCTION_NAME_KEY = "TWEET_PROCESSOR_FUNCTION_NAME";
    public static final String BATCH_SIZE_KEY = "BATCH_SIZE";

    private Env() {
    }

    public static String getConsumerKey() {
        return System.getenv(CONSUMER_KEY_KEY);
    }

    public static String getConsumerSecret() {
        return System.getenv(CONSUMER_SECRET_KEY);
    }

    public static String getAccessToken() {
        return System.getenv(ACCESS_TOKEN_KEY);
    }

    public static String getAccessTokenSecret() {
        return System.getenv(ACCESS_TOKEN_SECRET_KEY);
    }

    public static String getSearchText() {
        return System.getenv(SEARCH_TEXT_KEY);
    }

    public static String getSearchCheckpointTableName() {
        return System.getenv(SEARCH_CHECKPOINT_TABLE_NAME_KEY);
    }

    public static String getTweetProcessorFunctionName() {
        return System.getenv(TWEET_PROCESSOR_FUNCTION_NAME_KEY);
    }

    public static int getBatchSize() {
        return Integer.parseInt(System.getenv(BATCH_SIZE_KEY));
    }
}
