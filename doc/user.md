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

  .--------.
  | Course |
  '--------'

  .---------.
  | Journal |
  '---------'
```

Ablauf:

```
   . Applicant | Employee     | System
  ------------------------------------
   .             createUser
   .             createCourse
   . createUser

```
