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

  .-------------.  .-----------.
  | Information |  | Criterion |
  '-------------'  |-----------|
                   | evaluate  |
                   '-----------'

  .----------------------.  .-------------.
  | Course               |  | Application |
  |----------------------|  '-------------'
  | createAllocationRule |
  | apply                |
  '----------------------'

  .----------------.
  | AllocationRule |
  '----------------'

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
