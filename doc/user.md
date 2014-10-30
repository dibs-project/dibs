HUB-Benutzerdokumentation
=========================

Übersicht
---------

Struktur:

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
  | createAllocationRule | | createQuota    | | addRankingCriterion | | evaluate  |
  | apply                | '----------------' '---------------------' '-----------'
  | generateRankings     |
  | publish              |
  | unpublish            |
  '----------------------'

  .---------.
  | Journal |
  '---------'
```

Ablauf:

```
   . Applicant                            | Employee                        | System
  ----------------------------------------+---------------------------------+-----------------
   .                                      | ApplicationService.createUser   |
   .                                      | ApplicationService.createCourse |
   .                                      | Course.createAllocationRule     |
   .                                      | AllocationRule.createQuota      |
   .                                      | Quota.addRankingCriterion       |
   .                                      | Course.publish                  |
   . ApplicationService.register          |                                 |
   . User.createInformation               |                                 |
   . Course.apply                         |                                 |
   .                   		          |                                 | generateRankings

```
