dibs Developer Documentation
============================

## Development Environment Setup

To set up the test environment, copy `test.default.properties` to
`test.properties` and customize it to your needs.

dibs needs a PostgreSQL-Database for testing. By default we assume that there is a
local Database named "dibs_test". `README.md` describes how to set up a local Database.

To test the connection to DoSV we use integration tests.
In order to run those, you need to create an account on the test portal of Hochschulstart
and set BID and BAN in `test.properties`.

Debug messages are logged on level FINE. To display them set the `debug` property to `true`.

## Testing dibs

To run all tests:

    mvn test

To run only integration tests:

    mvn test-compile failsafe:integration-test failsafe:verify

## Run dibs Development Server

Start the dibs-Development Server:

    ./dibs.sh

## Design Conventions

* General principles: [KISS](http://en.wikipedia.org/wiki/KISS_principle),
  [DRY](http://en.wikipedia.org/wiki/Don%27t_repeat_yourself)
* *test-driven development*: Any sufficiently complex function needs a test case. Any function that
  relies on web APIs, is time-consuming or cannot be tested at any time needs an integration test.
* Utilities used should be self-contained
* *Object-oriented* structure that abstracts from the DB. DB access is contained within the object.

## Code Conventions

* *Be consistent*: Write readable and maintainable code. Look at other contributors' code and take
  it as orientation.
* *Getters* do not alter an object's (and its DB representation's) inner state and always return an
  object.
* *Method signatures* take object IDs.
* *Refactoring* is carried out in separate issues/branches and not mixed with features.
* Wrap object properties (i.e. getters) in a block between `/* ---- Properties ----*/` and
  `/* ---- /Properties ----/*`
* Mark subordinate overloaded methods with `// overload`
* HTML: [void elements](http://www.w3.org/TR/html5/syntax.html#void-elements) use self-closing
  notation, e.g. `<br />`.

We use Checkstyle to enforce code conventions (see `misc/checkstyle.xml`). If, in certain
circumstances, an exception from a rule is reasonable, mark the affected line with
`// checkstyle: ignore <check>, <reason>`, where `check` is the name of a
[Checkstyle check](http://checkstyle.sourceforge.net/checks.html) and `reason` is a tiny
description why the rule is broken here.
