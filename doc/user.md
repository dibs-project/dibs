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
  | retractPublication   |
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
   . register          |                      |
   . createInformation |                      |
   . apply             |                      |

```
