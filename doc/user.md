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

  .----------------------. .----------------. .-------. .-----------.
  | Course               | | AllocationRule | | Quota | | Criterion |
  |----------------------| '----------------' '-------' |-----------|
  | createAllocationRule |                              | evaluate  |
  | apply                |                              '-----------'
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
   . createUser        |                      |
   . createInformation |                      |
   . apply

```
