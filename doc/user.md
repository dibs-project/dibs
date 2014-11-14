HUB-User Documentation
======================

Overview
---------

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
  | generateRankings     |                    '---------------------'
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
   .                                      |                                 | Course.generateRankings

```

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
