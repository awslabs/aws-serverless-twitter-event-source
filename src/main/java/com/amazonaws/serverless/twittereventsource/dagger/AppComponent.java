package com.amazonaws.serverless.twittereventsource.dagger;

import javax.inject.Singleton;

import com.amazonaws.serverless.twittereventsource.TwitterSearchPoller;

import dagger.Component;

/**
 * Application component interface.
 */
@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    TwitterSearchPoller poller();
}
