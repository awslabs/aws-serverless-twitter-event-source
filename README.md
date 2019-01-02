## AWS Serverless Twitter Event Source

![Build Status](https://codebuild.us-east-1.amazonaws.com/badges?uuid=eyJlbmNyeXB0ZWREYXRhIjoiTldCYjBXdGhocTJkRmtGK2k0REFHVVNsRllyK2hKTmZNWWR2T1creHhsZWVTMCszNC9hSGcyWklFMWE5ZWN6NDVOZnV4WTN4ekV3NDFlcys2L3ZjRmN3PSIsIml2UGFyYW1ldGVyU3BlYyI6Ikgyd084eEVRdGUzNDY4djMiLCJtYXRlcmlhbFNldFNlcmlhbCI6MX0%3D&branch=master)

This serverless app turns a twitter search query into an AWS Lambda event source by invoking a given lambda function to process tweets found by search. It works by periodically polling the freely available public [Twitter Standard Search API](https://developer.twitter.com/en/docs/tweets/search/overview/standard) and invoking a lambda function you provide to process tweets found.

## Architecture

![App Architecture](https://github.com/awslabs/aws-serverless-twitter-event-source/raw/master/images/app-architecture.png)

1. The TwitterSearchPoller lambda function is periodically triggered by a CloudWatch Events Rule.
1. In stream mode, a DynamoDB table is used to keep track of a checkpoint, which is the latest tweet timestamp found by past searches.
1. The poller function calls the Twitter Standard Search API and searches for tweets using the search text provided as an app parameter.
1. The TweetProcessor lambda function (provided by the app user) is invoked with any new tweets that were found after the last checkpoint.
1. If stream mode is not enabled, the TweetProcessor lambda function will be invoked with all search results found, regardless of whether they had been seen before.
    1.  Note, the TweetProcessor function is invoked asynchronously (Event invocation type). The app does not confirm that the lambda was able to successfully process the tweets. If you're concerned about tweets being lost due to failures in your lambda function, you should configure a DLQ on your lambda function so failed messages will end up on the DLQ automatically. See the [AWS Lambda DLQ documentation](https://docs.aws.amazon.com/lambda/latest/dg/dlq.html) for more information.

## Installation Steps

This app is meant to be used as part of a larger application, so the recommended way to use it is to embed it as a nested app in your serverless application. To do this, paste the following into your SAM template:

```yaml
  TweetSource:
    Type: AWS::Serverless::Application
    Properties:
      Location:
        ApplicationId: arn:aws:serverlessrepo:us-east-1:077246666028:applications/aws-serverless-twitter-event-source
        SemanticVersion: 2.0.0
      Parameters:
        # Non-URL-encoded search text poller should use when querying Twitter Search API.
        SearchText: '#serverless -filter:nativeretweets'
        # Name of lambda function that should be invoked to process tweets. Note, this must be a function name and not a function ARN.
        TweetProcessorFunctionName: !Ref MyFunction
        # This app assumes API keys needed to use the Twitter API are stored as SecureStrings in SSM Parameter Store under the prefix
        # defined by this parameter. See the app README for details.
        #SSMParameterPrefix: twitter-event-source # Uncomment to override default value
        # Frequency in minutes to poll for more tweets.
        #PollingFrequencyInMinutes: 1 # Uncomment to override default value
        # Max number of tweets to send to the TweetProcessor lambda function on each invocation.
        #BatchSize: 15 # Uncomment to override default value
        # If true, the app will remember the last tweet found and only invoke the tweet processor function for newer tweets.
        # If false, the app will be stateless and invoke the tweet processor function with all tweets found in each polling cycle.
        #StreamModeEnabled: false # Uncomment to override default value
```

Alternatively, you can deploy the application into your account manually via the [aws-serverless-twitter-event-source SAR page](https://serverlessrepo.aws.amazon.com/applications/arn:aws:serverlessrepo:us-east-1:077246666028:applications~aws-serverless-twitter-event-source).

1. [Create an AWS account](https://portal.aws.amazon.com/gp/aws/developer/registration/index.html) if you do not already have one and login
1. Go to the app's page on the [Serverless Application Repository](https://serverlessrepo.aws.amazon.com/applications/arn:aws:serverlessrepo:us-east-1:077246666028:applications~aws-serverless-twitter-event-source) and click "Deploy"
1. Provide the required app parameters (see below for steps to create Twitter API parameters, e.g., Consumer Key)

### Twitter API Keys

The app requires the following Twitter API Keys: Consumer Key (API Key), Consumer Secret (API Secret), Access Token, and Access Token Secret. The following steps walk you through registering the app with your Twitter account to create these values.

1. Create a [Twitter](https://twitter.com/) account if you do not already have one
1. Register a new application with your Twitter account:
    1. Go to [http://twitter.com/oauth_clients/new](http://twitter.com/oauth_clients/new)
    1. Click "Create New App"
    1. Under Name, enter something descriptive (but unique), e.g., `aws-serverless-twitter-es`
    1. Enter a description
    1. Under Website, you can enter `https://github.com/awslabs/aws-serverless-twitter-event-source`
    1. Leave Callback URL blank
    1. Read and agree to the Twitter Developer Agreement
    1. Click "Create your Twitter application"
1. (Optional, but recommended) Restrict the application permissions to read only
    1. From the detail page of your Twitter application, click the "Permissions" tab
    1. Under the "Access" section, make sure "Read only" is selected and click the "Update Settings" button
1. Generate an access token:
    1. From the detail page of your Twitter application, click the "Keys and Access Tokens" tab
    1. On this tab, you will already see the Consumer Key (API Key) and Consumer Secret (API Secret) values required by the app.
    1. Scroll down to the Access Token section and click "Create my access token"
    1. You will now have the Access Token and Access Token Secret values required by the app.

### Twitter API Key Setup

The app expects to find the Twitter API keys as encrypted SecureString values in [SSM Parameter Store](https://docs.aws.amazon.com/systems-manager/latest/userguide/systems-manager-paramstore.html). You can setup the required parameters via the AWS Console or using the following AWS CLI commands:

```text
aws ssm put-parameter --name /twitter-event-source/consumer_key --value <your consumer key value> --type SecureString --overwrite
aws ssm put-parameter --name /twitter-event-source/consumer_secret --value <your consumer secret value> --type SecureString --overwrite
aws ssm put-parameter --name /twitter-event-source/access_token --value <your access token value> --type SecureString --overwrite
aws ssm put-parameter --name /twitter-event-source/access_token_secret --value <your access token secret value> --type SecureString --overwrite
```

## App Parameters

In addition to the Twitter API key parameters, the app also requires the following additional parameters:

1. `SearchText` (required) - This is the **non-URL-encoded** search text the app will use when polling the Twitter Standard Search API. See the [Search Tweets](https://developer.twitter.com/en/docs/tweets/search/guides/standard-operators) help page to understand the available features. The [Twitter Search page](https://twitter.com/search) is a good place to manually test different searches, although note the standard search API generally returns different results, because it only indexes a sampling of tweets.
1. `TweetProcessorFunctionName` (required) - This is the name (not ARN) of the lambda function that will process tweets generated by the app.
1. `SSMParameterPrefix` (optional) - The prefix (without the leading `/`) of the SSM parameter names where the Twitter API keys can be found. Note, if you override this value, you need to make sure you have put the keys in SSM parameter store with names matching your prefix. For example, if you override the prefix to `myprefix`, then you will need to store the Twitter API keys in parameters with names `/myprefix/consumer_key`, `/myprefix/consumer_secret`, `/myprefix/access_token`, `/myprefix/access_token_secret`. Default: twitter-event-source.
1. `PollingFrequencyInMinutes` (optional) - The frequency at which the lambda will poll the Twitter Search API (in minutes). Default: 1.
1. `BatchSize` (optional) - The max number of tweets that will be sent to the TweetProcessor lambda function in a single invocation. Default: 15.
1. `StreamModeEnabled` (optional) - If true, the app will save the latest timestamp of the previous tweets found and only invoke the tweet processor function for newer tweets. If false, the app will be stateless and invoke the tweet processor function with all tweets found in each polling cycle. Default: false.

## App Outputs

1. `TwitterSearchPollerFunctionName` - Name of the search poller Lambda function.
1. `TwitterSearchPollerFunctionArn` - ARN of the search poller Lambda function.
1. `SearchCheckpointTableName` - Name of the search checkpoint table.
1. `SearchCheckpointTableArn` - ARN of the search checkpoint table.

## App Interface

The aws-serverless-twitter-event-source app invokes the TweetProcessor function with a payload containing a JSON array of Tweet objects as they were returned from the Twitter search API. See the [Twitter API documentation](https://developer.twitter.com/en/docs/tweets/data-dictionary/overview/intro-to-tweet-json) for the format of tweet objects. A single invocation of the TweetProcessor will never receive more tweets than the configured batch size, but it may receive less.

The TweetProcessor is invoked asynchronously by the search poller, so there is no need to return a value from the TweetProcessor function.

## Upgrading from version 1.x

aws-serverless-twitter-event-source v2 contains breaking changes from v1 so apps wishing to upgrade from v1 to v2 need to make changes to their existing TweetProcessor Lambda function. The major changes are:

1. Added support for extended mode so longer tweets are no longer truncated. No special upgrade steps are necessary for this change.
1. The app was previously sending the tweets to the TweetProcessor as a JSON array of strings, where each string was the Tweet JSON. This meant, the TweetProcessor function had to deserialize the JSON instead of letting Lambda's native deserialization handle it. As part of upgrading, the TweetProcessor should be updated to expect that the payload will now be an array of JSON objects, rather than an array of strings containing the JSON object data.
1. Twitter API keys are now fetched from SSM Parameter Store instead of being passed in as app parameters. When upgrading, you must follow the installation steps above to install your Twitter API Keys as SSM Parameter Store SecureStrings **before** deploying the upgraded app.
1. When stream mode is enabled, the app now stores the last tweet id processed instead of a timestamp. This allows the app to take advantage of the Twitter API's native support for passing in the last tweet id to continue reading only tweets after that id. If you have stream mode enabled, you will have to perform the following steps to upgrade:
    1. Disable the CloudWatch Events Rule to stop the search poller from being invoked.
    1. Manually delete the "checkpoint" row in the SearchCheckpoint DynamoDB table.
    1. Re-enable the CloudWatch Events Rule to resume the search poller.

## License Summary

This sample code is made available under a modified MIT license. See the LICENSE file.
