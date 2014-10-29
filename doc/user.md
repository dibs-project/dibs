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
  '----------------------'

  .---------. .---------.
  | Session | | Journal |
  '---------' '---------'
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
