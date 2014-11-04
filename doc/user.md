HUB-Benutzerdokumentation
=========================

Übersicht
---------

Struktur:

```
  .--------------------. .----------. .-------------------.
  | ApplicationService | | Settings | | User              |
  |--------------------| '----------' |-------------------|
  | createUser         |              | createInformation |
  | createCourse       |              '-------------------'
  | register           |
  '--------------------'

  .-------------. .-------------. .------------.
  | Information | | Application | | Evaluation |
  '-------------' '-------------' '------------'

  .----------------------. .----------------. .---------------------. .-----------.
  | Course               | | AllocationRule | | Quota               | | Criterion |
  |----------------------| |----------------| |---------------------| |-----------|
  | createAllocationRule | | createQuota    | | addRankingCriterion | | evaluate  |
  | apply                | '----------------' '---------------------' '-----------'
  | publish              |
  | unpublish            |
  '----------------------'

  .---------. .---------.
  | Session | | Journal |
  '---------' '---------'
```

Ablauf:

```
   . Applicant                            | Employee                        | System
  ----------------------------------------+---------------------------------+-------
   .                                      | ApplicationService.createUser   |
   .                                      | ApplicationService.createCourse |
   .                                      | Course.createAllocationRule     |
   .                                      | AllocationRule.createQuota      |
   .                                      | Quota.addRankingCriterion       |
   .                                      | Course.publish                  |
   . ApplicationService.register          |                                 |
   . User.createInformation               |                                 |
   . Course.apply                         |                                 |

```
