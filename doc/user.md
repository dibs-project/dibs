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
  | publish              |
  | unpublish            |
  '----------------------'

  .---------.
  | Journal |
  '---------'
```

Ablauf:

```
   . Applicant         | Employee             | System
  ---------------------+----------------------+-------
   .                   | createUser           |
   .                   | createCourse         |
   .                   | createAllocationRule |
   .                   | createQuota          |
   .                   | addRankingCriterion  |
   .                   | Course.publish       |
   . register          |                      |
   . createInformation |                      |
   . apply             |                      |

```
