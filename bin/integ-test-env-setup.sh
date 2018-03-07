#!/bin/bash

#
# Run during pre-integration-test phase. Deploys integ test environment and test app stack. Assumes running as part of a Travis CI build.
#
# Assumes the following are installed on the build host: AWS CLI, jq
#
# Depends on the following env vars being set:
#  -TRAVIS_BUILD_ID - used to make stack names unique to this build
#  -AWS_DEFAULT_REGION - region in which to run integ tests
#  -AWS_ACCESS_KEY_ID - AWS access key id used for tests
#  -AWS_SECRET_ACCESS_KEY - AWS secret access key used for tests
#  -PACKAGING_S3_BUCKET - S3 bucket used for packaging the app code artifacts
#  -TWITTER_CONSUMER_KEY - Twitter API consumer key
#  -TWITTER_CONSUMER_SECRET - Twitter API consumer secret
#  -TWITTER_ACCESS_TOKEN - Twitter API access token
#  -TWITTER_ACCESS_TOKEN_SECRET - Twitter API access token secret
#  -TWITTER_SEARCH_TEXT - Search text to use when querying Twitter Search API
#

set -e # fail script on any individual command failing
shopt -s nullglob

export LANG=en_US.UTF-8

test_environment_stack_name="integ-test-environment-${TRAVIS_BUILD_ID}"
app_stack_name="integ-test-app-${TRAVIS_BUILD_ID}"

echo "Deploying test environment stack: $test_environment_stack_name"
aws cloudformation deploy \
  --template-file src/test/resources/integ-test-environment.yml \
  --stack-name $test_environment_stack_name \
  --capabilities CAPABILITY_IAM

test_environment_stack_outputs=$(aws cloudformation describe-stacks --stack-name $test_environment_stack_name | jq -e '.Stacks[0].Outputs')
tweet_processor_function_name=$(echo $test_environment_stack_outputs | jq -er '.[] | select(.OutputKey == "TweetProcessorFunctionName") | .OutputValue')

echo "Packaging app template"
output_template_path=target/package_template.yml
aws cloudformation package \
  --template-file app.template.yml \
  --output-template-file $output_template_path \
  --s3-bucket $PACKAGING_S3_BUCKET

echo "Deploying app stack: $app_stack_name"
aws cloudformation deploy \
  --template-file $output_template_path \
  --stack-name $app_stack_name \
  --capabilities CAPABILITY_IAM \
  --parameter-overrides \
    ConsumerKey="$TWITTER_CONSUMER_KEY" \
    ConsumerSecret="$TWITTER_CONSUMER_SECRET" \
    AccessToken="$TWITTER_ACCESS_TOKEN" \
    AccessTokenSecret="$TWITTER_ACCESS_TOKEN_SECRET" \
    SearchText="$TWITTER_SEARCH_TEXT" \
    TweetProcessorFunctionName="$tweet_processor_function_name"
