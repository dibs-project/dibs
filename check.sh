#!/bin/sh

# Run the dibs code quality checks. The return code is 0 if the checks pass, 1 if there are any
# issues.

set -e

echo Linting...
mvn checkstyle:check
echo "\nRunning unit tests..."
mvn test
echo "\nRunning integration tests..."
mvn failsafe:integration-test failsafe:verify
echo "\nEverything looks fine, good work!"
