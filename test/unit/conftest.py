import pytest
from unittest.mock import MagicMock

# make sure we can find the app code
import sys
import os

import test_constants

my_path = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, my_path + '/../../app/')

# set expected config environment variables to test constants
os.environ['AWS_DEFAULT_REGION'] = test_constants.REGION
os.environ['SEARCH_CHECKPOINT_TABLE_NAME'] = test_constants.SEARCH_CHECKPOINT_TABLE_NAME
os.environ['SEARCH_TEXT'] = test_constants.SEARCH_TEXT
os.environ['TWEET_PROCESSOR_FUNCTION_NAME'] = test_constants.TWEET_PROCESSOR_FUNCTION_NAME
os.environ['BATCH_SIZE'] = test_constants.BATCH_SIZE
os.environ['STREAM_MODE_ENABLED'] = test_constants.STREAM_MODE_ENABLED
os.environ['SSM_PARAMETER_PREFIX'] = test_constants.SSM_PARAMETER_PREFIX


@pytest.fixture
def mock_twitter_proxy(mocker):
    mock_client = MagicMock()
    mock_client.get_parameters.return_value = {
        'Parameters': [
            {
                'Name': '/{}/consumer_key'.format(test_constants.SSM_PARAMETER_PREFIX),
                'Value': test_constants.CONSUMER_KEY
            },
            {
                'Name': '/{}/consumer_secret'.format(test_constants.SSM_PARAMETER_PREFIX),
                'Value': test_constants.CONSUMER_SECRET
            },
            {
                'Name': '/{}/access_token'.format(test_constants.SSM_PARAMETER_PREFIX),
                'Value': test_constants.ACCESS_TOKEN
            },
            {
                'Name': '/{}/access_token_secret'.format(test_constants.SSM_PARAMETER_PREFIX),
                'Value': test_constants.ACCESS_TOKEN_SECRET
            }
        ],
        'InvalidParameters': []
    }
    import boto3
    mocker.patch.object(boto3, 'client')
    boto3.client.return_value = mock_client
    import twitter_proxy
