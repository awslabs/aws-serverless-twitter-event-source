package com.amazonaws.serverless.twittereventsource.lambda;

import com.amazonaws.serverless.twittereventsource.TwitterSearchPoller;
import com.amazonaws.serverless.twittereventsource.dagger.AppComponent;
import com.amazonaws.serverless.twittereventsource.dagger.DaggerAppComponent;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent;

import lombok.RequiredArgsConstructor;

/**
 * Lambda request handler for scheduled poller event.
 */
@RequiredArgsConstructor
public class TwitterSearchPollerHandler implements RequestHandler<ScheduledEvent, Void> {
    private final TwitterSearchPoller poller;

    public TwitterSearchPollerHandler() {
        AppComponent component = DaggerAppComponent.create();
        poller = component.poller();
    }

    @Override
    public Void handleRequest(final ScheduledEvent scheduledEvent, final Context context) {
        poller.poll();
        return null;
    }
}
