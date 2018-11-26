import pytest

from boto3.dynamodb.conditions import Attr, Or
from botocore.exceptions import ClientError

import checkpoint


def test_last_id_no_record(mocker):
    mocker.patch.object(checkpoint, 'TABLE')
    checkpoint.TABLE.get_item.return_value = {}
    assert checkpoint.last_id() is None
    checkpoint.TABLE.get_item.assert_called_with(
        Key={'id': checkpoint.RECORD_KEY}
    )


def test_last_id_record_exists(mocker):
    mocker.patch.object(checkpoint, 'TABLE')
    checkpoint.TABLE.get_item.return_value = {'Item': {'since_id': 5}}
    assert checkpoint.last_id() == 5


def test_update(mocker):
    mocker.patch.object(checkpoint, 'TABLE')
    checkpoint.update(5)
    checkpoint.TABLE.put_item.assert_called_with(
        Item={
            'id': checkpoint.RECORD_KEY,
            'since_id': 5
        },
        ConditionExpression=Or(
                Attr('id').not_exists(),
                Attr('since_id').lt(5)
        )
    )


def test_update_condition_fails(mocker):
    mocker.patch.object(checkpoint, 'TABLE')
    checkpoint.TABLE.put_item.side_effect = ClientError(
        {
            'Error': {'Code': 'ConditionalCheckFailedException'}
        },
        'PutItem'
    )
    checkpoint.update(5)


def test_update_other_error_code(mocker):
    mocker.patch.object(checkpoint, 'TABLE')
    checkpoint.TABLE.put_item.side_effect = ClientError(
        {
            'Error': {'Code': 'SomethingElse'}
        },
        'PutItem'
    )
    with pytest.raises(ClientError):
        checkpoint.update(5)
