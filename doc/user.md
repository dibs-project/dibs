HUB-Benutzerdokumentation
=========================

Übersicht
---------

Struktur:

```
  .--------------------.  .----------.  .------.
  | ApplicationService |  | Settings |  | User |
  |--------------------|  '----------'  '------'
  | createUser         |
  | createCourse       |
  '--------------------'

  .-------------.
  | Information |
  '-------------'

  .----------------------.
  | Course               |
  |----------------------|
  | createAllocationRule |
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
   . createInformation |

```
