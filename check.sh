#!/bin/sh

# Run the dibs code quality checks. The return code is 0 if the checks pass, 1 if there are any
# issues.

set -e

echo Linting...
# TODO: mvn checkstyle:check
mvn checkstyle:check -Dcheckstyle.includes="**/ApplicationService.java, **/User.java,\
    **/AllocationRule.java, **/Application.java, **/Course.java, **/Qualification.java,\
    **/Quota.java, **/DosvSync.java, **/ApplicationServiceTest.java, **/ApplicationTest.java"
echo "\nRunning unit tests..."
mvn test
echo "\nRunning integration tests..."
mvn failsafe:integration-test failsafe:verify
echo "\nEverything looks fine, good work!"
