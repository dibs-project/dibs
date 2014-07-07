HUB-Benutzerdokumentation
=========================

Übersicht
---------

Struktur:

```
  .--------------------.  .----------.  .-------------------.
  | ApplicationService |  | Settings |  | User              |
  |--------------------|  '----------'  |-------------------|
  | createUser         |                | createInformation |
  | createCourse       |                '-------------------'
  '--------------------'

  .-------------. .-------------. 
  | Information | | Application | 
  '-------------' '-------------' 

  .----------------------. .----------------. .--------------. .-----------.
  | Course               | | AllocationRule | | Quota        | | Criterion |
  |----------------------| |----------------| |--------------' |-----------|
  | createAllocationRule | | createQuota    | | addCriterion | | evaluate  |
  | apply                | '----------------' '--------------' '-----------'
  '----------------------'

  .---------.
  | Journal |
  '---------'
```

Ablauf:

```
   . Applicant         | Employee             | System
  ----------------------------------------------------
   .                   | createUser           |
   .                   | createCourse         |
   .                   | createAllocationRule |
   .                   | createQuota          |
   .                   | addCriterion         |
   . createUser        |                      |
   . createInformation |                      |
   . apply             |                      |

```
