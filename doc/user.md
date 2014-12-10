HUB-User Documentation
======================

Overview
---------

HUB is a system for receiving university applications, evaluating application information
and applicant ranking. It is built as a web application.

Structure:

```
  .--------------------. .----------. .-------------------.
  | ApplicationService | | Settings | | User              |
  |--------------------| '----------' |-------------------|
  | register           |              | createInformation |
  | createUser         |              '-------------------'
  | createCourse       |
  '--------------------'

  .-------------. .-------------. .------------.
  | Information | | Application | | Evaluation |
  '-------------' '-------------' '------------'

  .----------------------. .----------------. .---------------------. .-----------.
  | Course               | | AllocationRule | | Quota               | | Criterion |
  |----------------------| |----------------| |---------------------| |-----------|
  | createAllocationRule | | createQuota    | | addRankingCriterion | |evaluate   |
  | apply                | '----------------' | generateRanking     | '-----------'
  | startAdmission       |                    '---------------------'
  | generateRankings     |
  | publish              |
  | unpublish            |
  '----------------------'

  .---------.
  | Journal |
  '---------'
```

Process:

```
   . Applicant                            | Employee                        | System
  ----------------------------------------+---------------------------------+------------------------
   .                                      | ApplicationService.createUser   |
   .                                      | ApplicationService.createCourse |
   .                                      | Course.createAllocationRule     |
   .                                      | AllocationRule.createQuota      |
   .                                      | Quota.addRankingCriterion       |
   .                                      | Course.publish                  |
   . ApplicationService.register          |                                 |
   . User.createInformation               |                                 |
   . Course.apply                         |                                 |
   .                                      | Course.startAdmission           | Course.generateRankings
```

Backend
-------

The HUB backend is located in the `de.huberlin.cms.hub`. DoSV-specific backend classes are
contained in `de.huberlin.cms.hub.dosv`. The root class of the backend is `ApplicationService`,
which contains the database connection, access to configuration and settings and all `HubObjects`
via their ID. `HubObject` is the base class for all objects that are stored in the database.
Every `HubObject` contains an instance of `ApplicationService` to be used for database access.
Objects are created via factory methods. Storage and retrieval of object information is
handled by the creator and modification methods in HUB. Top level objects are created in
the `ApplicationService` while other objects are created from the classes they are logically
connected to.

The backend uses

 * [PostgreSQL](http://www.postgresql.org/)
 * [Apache Commons DbUtils](http://commons.apache.org/proper/commons-dbutils/)

User Interface
--------------

The user interface code is located in the `de.huberlin.cms.hub.ui` package. `Ui` is the
main class and primarily responsible for initializing the user interface. All pages (i.e.
views) are implemented in the `Pages` class. A page is usually just a thin wrapper around
a backend method.

The user interface is built upon:

 * [Java Servlet](https://java.net/projects/servlet-spec/)
 * [Jersey](https://jersey.java.net/)
 * [FreeMarker](http://freemarker.org/)

Web application resources are located at `src/main/webapp`. Templates can be found in the
`templates` subdirectory.
