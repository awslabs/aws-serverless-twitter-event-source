import pytest

import json
import os

import test_constants


@pytest.fixture
def poller(mocker, mock_twitter_proxy):
    import poller
    mocker.patch.object(poller, 'LAMBDA')
    mocker.patch.object(poller, 'checkpoint')
    mocker.patch.object(poller, 'twitter_proxy')
    return poller


def test_handler_single_batch(poller):
    tweets = [
        {'id': 1},
        {'id': 2}
    ]
    poller.twitter_proxy.search.side_effect = [
        {
            'statuses': tweets,
            'search_metadata': {
                'max_id': 2
            }
        },
        {
            'statuses': []
        }
    ]

    poller.handler(None, None)

    assert poller.twitter_proxy.search.call_count == 2
    poller.twitter_proxy.search.assert_any_call(test_constants.SEARCH_TEXT, None)

    poller.LAMBDA.invoke.assert_called_once_with(
        FunctionName=test_constants.TWEET_PROCESSOR_FUNCTION_NAME,
        InvocationType='Event',
        Payload=json.dumps(tweets)
    )
    poller.checkpoint.last_id.assert_not_called()
    poller.checkpoint.update.assert_not_called()


def test_handler_multiple_batches(poller):
    tweets = [
        {'id': 1},
        {'id': 2},
        {'id': 3},
        {'id': 4},
        {'id': 5}
    ]
    poller.twitter_proxy.search.side_effect = [
        {
            'statuses': tweets,
            'search_metadata': {
                'max_id': 5
            }
        },
        {
            'statuses': []
        }
    ]
    poller.handler(None, None)

    assert poller.twitter_proxy.search.call_count == 2
    poller.twitter_proxy.search.assert_any_call(test_constants.SEARCH_TEXT, None)

    assert poller.LAMBDA.invoke.call_count == 3
    poller.LAMBDA.invoke.assert_any_call(
        FunctionName=test_constants.TWEET_PROCESSOR_FUNCTION_NAME,
        InvocationType='Event',
        Payload=json.dumps([{'id': 1}, {'id': 2}])
    )
    poller.LAMBDA.invoke.assert_any_call(
        FunctionName=test_constants.TWEET_PROCESSOR_FUNCTION_NAME,
        InvocationType='Event',
        Payload=json.dumps([{'id': 3}, {'id': 4}])
    )
    poller.LAMBDA.invoke.assert_any_call(
        FunctionName=test_constants.TWEET_PROCESSOR_FUNCTION_NAME,
        InvocationType='Event',
        Payload=json.dumps([{'id': 5}])
    )
    poller.checkpoint.last_id.assert_not_called()
    poller.checkpoint.update.assert_not_called()


def test_handler_stream_mode_enabled(mocker, poller):
    mocker.patch.object(poller, 'STREAM_MODE_ENABLED')
    poller.STREAM_MODE_ENABLED = True

    poller.checkpoint.last_id.return_value = 3

    tweets = [
        {'id': 4},
        {'id': 5}
    ]
    poller.twitter_proxy.search.side_effect = [
        {
            'statuses': tweets,
            'search_metadata': {
                'max_id': 5
            }
        },
        {
            'statuses': []
        }
    ]

    poller.handler(None, None)

    assert poller.twitter_proxy.search.call_count == 2
    poller.twitter_proxy.search.assert_any_call(test_constants.SEARCH_TEXT, 3)
    poller.twitter_proxy.search.assert_any_call(test_constants.SEARCH_TEXT, 5)

    poller.checkpoint.last_id.assert_called_once()
    poller.checkpoint.update.assert_called_once_with(5)
