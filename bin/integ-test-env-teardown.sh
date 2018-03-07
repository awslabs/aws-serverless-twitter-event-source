#!/bin/bash

#
# Run during post-integration-test phase. Deletes integ test stacks. Assumes running as part of a Travis CI build.
#
# Assumes the following are installed on the build host: AWS CLI, jq
#
# Depends on the following env vars being set:
#  -TRAVIS_BUILD_ID - used to make stack names unique to this build
#  -AWS_DEFAULT_REGION - region in which to run integ tests
#  -AWS_ACCESS_KEY_ID - AWS access key id used for tests
#  -AWS_SECRET_ACCESS_KEY - AWS secret access key used for tests
#

set -e # fail script on any individual command failing
shopt -s nullglob

export LANG=en_US.UTF-8

test_environment_stack_name="integ-test-environment-${TRAVIS_BUILD_ID}"
app_stack_name="integ-test-app-${TRAVIS_BUILD_ID}"

echo "Deleting app stack: $app_stack_name"
aws cloudformation delete-stack --stack-name $app_stack_name

echo "Deleting test environment stack: $test_environment_stack_name"
aws cloudformation delete-stack --stack-name $test_environment_stack_name
