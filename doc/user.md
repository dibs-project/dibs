HUB-Benutzerdokumentation
=========================

Übersicht
---------

Struktur:

```
  .--------------------.  .----------.  .-------------------.
  | ApplicationService |  | Settings |  | User              |
  |--------------------|  '----------'  |-------------------|
  | register           |                | createInformation |
  | createUser         |                '-------------------'
  | createCourse       | 
  '--------------------'

  .-------------. .-------------. 
  | Information | | Application | 
  '-------------' '-------------' 

  .----------------------. .----------------. .---------------------. .-----------.
  | Course               | | AllocationRule | | Quota               | | Criterion |
  |----------------------| |----------------| |---------------------| |-----------|
  | createAllocationRule | | createQuota    | | addRankingCriterion | | evaluate  |
  | apply                | '----------------' '---------------------' '-----------'
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
