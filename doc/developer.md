HUB Developer Documentation
===========================

Development Environment Setup
-----------------------------

To set up the test environment, copy `test.default.properties` to
`test.properties` and customize it to your needs.

HUB needs a PostgreSQL-Database for testing. By default we assume that there is a 
local Database named "hub_test". `README.md` describes how to set up a local Database.

To test the connection to DoSV we use integration tests. 
In order to run those, you need to create an account on the test portal of Hochschulstart
and set BID and BAN in `test.properties`.

Testing HUB 
-----------

To run all tests:

    mvn test
    
To run only integration tests:

    mvn test-compile failsafe:integration-test

Run HUB Development Server 
--------------------------

Start the HUB-Development Server:

    ./hub.sh