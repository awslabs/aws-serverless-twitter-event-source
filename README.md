## AWS Serverless Twitter Event Source

This serverless app turns a twitter search query into an AWS Lambda event source by invoking a given lambda function to process tweets found by search. It works by periodically polling the freely available public [Twitter Standard Search API](https://developer.twitter.com/en/docs/tweets/search/overview/standard) and invoking a lambda function you provide to process tweets found.

## Architecture

![App Architecture](https://github.com/awslabs/aws-serverless-twitter-event-source/raw/master/images/app-architecture.png)

1. The TwitterSearchPoller lambda function is periodically triggered by a CloudWatch Events Rule.
1. In stream mode, a DynamoDB table is used to keep track of a checkpoint, which is the latest tweet timestamp found by past searches.
1. The poller function calls the Twitter Standard Search API and searches for tweets using the search text provided as an app parameter.
1. The TweetProcessor lambda function (provided by the app user) is invoked with any new tweets that were found after the checkpoint timestamp.
1. If stream mode is not enabled, the TweetProcessor lambda function will be invoked with all search results found, regardless of whether they had been seen before.
    1.  Note, the TweetProcessor function is invoked asynchronously (Event invocation type). The app does not confirm that the lambda was able to successfully process the tweets. If you're concerned about tweets being lost due to failures in your lambda function, you should configure a DLQ on your lambda function so failed messages will end up on the DLQ automatically. See the [AWS Lambda DLQ documentation](https://docs.aws.amazon.com/lambda/latest/dg/dlq.html) for more information.

## Installation Steps

1. [Create an AWS account](https://portal.aws.amazon.com/gp/aws/developer/registration/index.html) if you do not already have one and login
1. Go to the app's page on the [Serverless Application Repository](https://serverlessrepo.aws.amazon.com/applications/arn:aws:serverlessrepo:us-east-1:077246666028:applications~aws-serverless-twitter-event-source) and click "Deploy"
1. Provide the required app parameters (see below for steps to create Twitter API parameters, e.g., Consumer Key)

### Twitter API Key Parameters

The app requires the following Twitter API parameters: Consumer Key (API Key), Consumer Secret (API Secret), Access Token, and Access Token Secret. The following steps walk you through registering the app with your Twitter account to create these values.

1. Create a [Twitter](https://twitter.com/) account if you do not already have one
1. Register a new application with your Twitter account:
    1. Go to [http://twitter.com/oauth_clients/new](http://twitter.com/oauth_clients/new)
    1. Click "Create New App"
    1. Under Name, enter something descriptive, e.g., `aws-serverless-twitter-es`
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

#### Encrypting Twitter API Key Parameters

Once you've created your Twitter API keys, you can copy them as plain text into the `PlaintextConsumerKey`, `PlaintextConsumerSecret`, `PlaintextAccessToken`, and `PlaintextAccessTokenSecret` parameters of the serverless application. However, it is **highly recommended** that you do NOT pass these values in plain text and instead encrypt them using an [AWS Key Management Service (KMS)](https://aws.amazon.com/kms/) key. Once encrypted, you put the encrypted values into the `EncryptedConsumerKey`, `EncryptedConsumerSecret`, `EncryptedAccessToken`, and `EncryptedAccessTokenSecret` parameters and provide the `DecryptionKeyName` parameter as well. The reason the Plaintext fields are provided at all is so this app can be used in regions that do not support AWS KMS.

The following subsections walk you through how to create a KMS key using the AWS console and encrypt your Twitter API Keys using the AWS CLI.

##### Create a new KMS Key

1. Login to the [AWS IAM console](http://console.aws.amazon.com/iam/home).
1. Click the "Encryption keys" menu item.
1. (Important) Just below the "Create key" button, there will be a Region selected. Change this to be the same region that you will deploy your app to.
1. Click "Create key".
1. Enter an alias, e.g., "twitter-api" and click "Next Step".
1. Click "Next Step" again to skip the add tags step.
1. Select a role that is allowed to administer the key, e.g., delete it, and click "Next Step".
1. Select a role that is allowed to use the key, e.g., encrypt with it, and click "Next Step".
1. Preview the key policy and then click "Finish".
1. Click on your newly created key and copy its full ARN value.

##### Encrypt Twitter API parameters with the AWS CLI

1. [Install the AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/installing.html).
1. Encrypt all 4 of your Twitter API keys by running this command for each key: `aws kms encrypt --key-id <key ARN> --plaintext '<Twitter API key>'`
1. The result JSON for each call will contain a field called `CiphertextBlob`. That string value (without the double-quotes) is what should be provided into the corresponding encrypted Twitter API key parameter of the serverless app.

### Other Parameters

In addition to the Twitter API key parameters, the app also requires the following additional parameters:

1. `SearchText` (required) - This is the **non-URL-encoded** search text the app will use when polling the Twitter Standard Search API. See the [Search Tweets](https://developer.twitter.com/en/docs/tweets/search/guides/standard-operators) help page to understand the available features. The [Twitter Search page](https://twitter.com/search) is a good place to manually test different searches, although note the standard search API generally returns different results, because it only indexes a sampling of tweets.
1. `TweetProcessorFunctionName` (required) - This is the name (not ARN) of the lambda function that will process tweets generated by the app.
1. `DecryptionKeyName` (required if providing encrypted Twitter API keys) - This is the KMS key name of the key used to encrypt the Twitter API parameters. Note, this must be just the key name (UUID that comes after `key/` in the key ARN), not the full key ARN. It's assumed the key was created in the same account and region as the app deployment.
1. `PollingFrequencyInMinutes` (optional) - The frequency at which the lambda will poll the Twitter Search API (in minutes). Default: 1.
1. `BatchSize` (optional) - The max number of tweets that will be sent to the TweetProcessor lambda function in a single invocation. Default: 15.
1. `StreamModeEnabled` (optional) - If true, the app will save the latest timestamp of the previous tweets found and only invoke the tweet processor function for newer tweets. If false, the app will be stateless and invoke the tweet processor function with all tweets found in each polling cycle. Default: false.

## License Summary

This sample code is made available under a modified MIT license. See the LICENSE file.
