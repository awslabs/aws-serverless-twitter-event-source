"""Manages search checkpoint (used when stream mode enabled)."""

import os

import boto3
from boto3.dynamodb.conditions import Attr, Or
from botocore.exceptions import ClientError

DDB = boto3.resource('dynamodb')
TABLE = DDB.Table(os.getenv('SEARCH_CHECKPOINT_TABLE_NAME'))
RECORD_KEY = 'checkpoint'


def last_id():
    """Return last checkpoint tweet id."""
    result = TABLE.get_item(
        Key={'id': RECORD_KEY}
    )
    if 'Item' in result:
        return result['Item']['since_id']
    return None


def update(since_id):
    """Update checkpoint to given tweet id."""
    try:
        TABLE.put_item(
            Item={
                'id': RECORD_KEY,
                'since_id': since_id
            },
            ConditionExpression=Or(
                Attr('id').not_exists(),
                Attr('since_id').lt(since_id)
            )
        )
    except ClientError as e:
        if e.response['Error']['Code'] != 'ConditionalCheckFailedException':
            raise
