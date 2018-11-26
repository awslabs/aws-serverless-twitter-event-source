"""Twitter API Helper."""

import os

import boto3
import twitter

SSM_PARAMETER_PREFIX = os.getenv("SSM_PARAMETER_PREFIX")
CONSUMER_KEY_PARAM_NAME = '/{}/consumer_key'.format(SSM_PARAMETER_PREFIX)
CONSUMER_SECRET_PARAM_NAME = '/{}/consumer_secret'.format(SSM_PARAMETER_PREFIX)
ACCESS_TOKEN_PARAM_NAME = '/{}/access_token'.format(SSM_PARAMETER_PREFIX)
ACCESS_TOKEN_SECRET_PARAM_NAME = '/{}/access_token_secret'.format(SSM_PARAMETER_PREFIX)

SSM = boto3.client('ssm')


def search(search_text, since_id=None):
    """Search for tweets matching the given search text."""
    return TWITTER.GetSearch(term=search_text, count=100, return_json=True, since_id=since_id)


def _create_twitter_api():
    parameter_names = [
            CONSUMER_KEY_PARAM_NAME,
            CONSUMER_SECRET_PARAM_NAME,
            ACCESS_TOKEN_PARAM_NAME,
            ACCESS_TOKEN_SECRET_PARAM_NAME
    ]
    result = SSM.get_parameters(
        Names=parameter_names,
        WithDecryption=True
    )

    if result['InvalidParameters']:
        raise RuntimeError(
            'Could not find expected SSM parameters containing Twitter API keys: {}'.format(parameter_names))

    param_lookup = {param['Name']: param['Value'] for param in result['Parameters']}
    return twitter.Api(
        consumer_key=param_lookup[CONSUMER_KEY_PARAM_NAME],
        consumer_secret=param_lookup[CONSUMER_SECRET_PARAM_NAME],
        access_token_key=param_lookup[ACCESS_TOKEN_PARAM_NAME],
        access_token_secret=param_lookup[ACCESS_TOKEN_SECRET_PARAM_NAME],
        tweet_mode='extended'
    )


TWITTER = _create_twitter_api()
