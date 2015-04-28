dibs Developer Documentation
===========================

Development Environment Setup
-----------------------------

To set up the test environment, copy `test.default.properties` to
`test.properties` and customize it to your needs.

dibs needs a PostgreSQL-Database for testing. By default we assume that there is a
local Database named "dibs_test". `README.md` describes how to set up a local Database.

To test the connection to DoSV we use integration tests. 
In order to run those, you need to create an account on the test portal of Hochschulstart
and set BID and BAN in `test.properties`.

Testing dibs 
-----------

To run all tests:

    mvn test

To run only integration tests:

    mvn test-compile failsafe:integration-test failsafe:verify

Run dibs Development Server 
--------------------------

Start the dibs-Development Server:

    ./dibs.sh

Design Conventions
------------------
   * General principles: [KISS](http://en.wikipedia.org/wiki/KISS_principle),
   [DRY](http://en.wikipedia.org/wiki/Don%27t_repeat_yourself)
   * *test-driven development*: Any sufficiently complex function needs a test case. Any
   function that relies on web APIs, is time-consuming or cannot be tested at any time
   needs an integration test.
   * Utilities used should be self-contained
   * *Object-oriented* structure that abstracts from the DB. DB access is contained within
   the object.

Code Conventions
----------------
   * *Be consistent*: Write readable and maintainable code. Look at other contributors' code
   and take it as orientation.
   * *Line length*: 90 characters.
   * *Getters* do not alter an object's (and its DB representation's) inner state and always
   return an object.
   * *Method signatures* take object IDs.
   * *No wild card imports* to prevent naming conflicts and bugs introduced by third party
   API changes.
   * *Refactoring* is carried out in separate issues/branches and not mixed with features.
