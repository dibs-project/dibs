Development Environment Setup
-----------------------------

To set up the Test Environment, copy `test.default.properties` to
`test.properties` and customize it to your needs.

HUB needs a PostgreSQL-Datenbank for Testing. By default we assume that there is a 
local Database named "hub_test". `README.md` its described how to Setup a Database.

Testing HUB 
-----------

To run all tests:

    mvn test

start HUB-Development Server 
----------------------------

Start the HUB-Development Server:

    ./hub.sh