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
   . register          |                      |
   . createInformation |                      |
   . apply

```
