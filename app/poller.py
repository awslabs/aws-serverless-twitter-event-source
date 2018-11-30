"""Lambda handler for polling twitter API with configured search."""

import lambdainit  # noqa: F401

import json
import os

import boto3

import twitter_proxy
import checkpoint

LAMBDA = boto3.client('lambda')

SEARCH_TEXT = os.getenv('SEARCH_TEXT')
TWEET_PROCESSOR_FUNCTION_NAME = os.getenv('TWEET_PROCESSOR_FUNCTION_NAME')
BATCH_SIZE = int(os.getenv('BATCH_SIZE'))
STREAM_MODE_ENABLED = os.getenv('STREAM_MODE_ENABLED') == 'true'


def handler(event, context):
    """Forward SQS messages to Kinesis Firehose Delivery Stream."""
    for batch in _search_batches():
        LAMBDA.invoke(
            FunctionName=TWEET_PROCESSOR_FUNCTION_NAME,
            InvocationType='Event',
            Payload=json.dumps(batch)
        )


def _search_batches():
    since_id = None
    if STREAM_MODE_ENABLED:
        since_id = checkpoint.last_id()

    tweets = []
    while True:
        result = twitter_proxy.search(SEARCH_TEXT, since_id)
        if not result['statuses']:
            # no more results
            break

        tweets = result['statuses']
        size = len(tweets)
        for i in range(0, size, BATCH_SIZE):
            yield tweets[i:min(i + BATCH_SIZE, size)]
        since_id = result['search_metadata']['max_id']

        if STREAM_MODE_ENABLED:
            checkpoint.update(since_id)
