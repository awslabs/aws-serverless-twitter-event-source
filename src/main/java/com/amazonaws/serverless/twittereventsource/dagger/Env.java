package com.amazonaws.serverless.twittereventsource.dagger;

/**
 * Helper class for fetching environment values.
 */
public final class Env {
    public static final String ENCRYPTED_CONSUMER_KEY_KEY = "ENCRYPTED_CONSUMER_KEY";
    public static final String ENCRYPTED_CONSUMER_SECRET_KEY = "ENCRYPTED_CONSUMER_SECRET";
    public static final String ENCRYPTED_ACCESS_TOKEN_KEY = "ENCRYPTED_ACCESS_TOKEN";
    public static final String ENCRYPTED_ACCESS_TOKEN_SECRET_KEY = "ENCRYPTED_ACCESS_TOKEN_SECRET";
    public static final String PLAINTEXT_CONSUMER_KEY_KEY = "PLAINTEXT_CONSUMER_KEY";
    public static final String PLAINTEXT_CONSUMER_SECRET_KEY = "PLAINTEXT_CONSUMER_SECRET";
    public static final String PLAINTEXT_ACCESS_TOKEN_KEY = "PLAINTEXT_ACCESS_TOKEN";
    public static final String PLAINTEXT_ACCESS_TOKEN_SECRET_KEY = "PLAINTEXT_ACCESS_TOKEN_SECRET";
    public static final String SEARCH_TEXT_KEY = "SEARCH_TEXT";
    public static final String SEARCH_CHECKPOINT_TABLE_NAME_KEY = "SEARCH_CHECKPOINT_TABLE_NAME";
    public static final String TWEET_PROCESSOR_FUNCTION_NAME_KEY = "TWEET_PROCESSOR_FUNCTION_NAME";
    public static final String BATCH_SIZE_KEY = "BATCH_SIZE";
    private static final String STREAM_MODE_ENABLED_KEY = "STREAM_MODE_ENABLED";

    private Env() {
    }

    public static String getEncryptedConsumerKey() {
        return System.getenv(ENCRYPTED_CONSUMER_KEY_KEY);
    }

    public static String getEncryptedConsumerSecret() {
        return System.getenv(ENCRYPTED_CONSUMER_SECRET_KEY);
    }

    public static String getEncryptedAccessToken() {
        return System.getenv(ENCRYPTED_ACCESS_TOKEN_KEY);
    }

    public static String getEncryptedAccessTokenSecret() {
        return System.getenv(ENCRYPTED_ACCESS_TOKEN_SECRET_KEY);
    }

    public static String getPlaintextConsumerKey() {
        return System.getenv(PLAINTEXT_CONSUMER_KEY_KEY);
    }

    public static String getPlaintextConsumerSecret() {
        return System.getenv(PLAINTEXT_CONSUMER_SECRET_KEY);
    }

    public static String getPlaintextAccessToken() {
        return System.getenv(PLAINTEXT_ACCESS_TOKEN_KEY);
    }

    public static String getPlaintextAccessTokenSecret() {
        return System.getenv(PLAINTEXT_ACCESS_TOKEN_SECRET_KEY);
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

    public static boolean isStreamModeEnabled() {
        return Boolean.valueOf(System.getenv(STREAM_MODE_ENABLED_KEY));
    }
}
