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

  .-------------.
  | Information |
  '-------------'

  .--------.
  | Course |
  '--------'

  .---------.
  | Journal |
  '---------'
```

Ablauf:

```
   . Applicant         | Employee     | System
  --------------------------------------------
   .                     createUser
   .                     createCourse
   . createUser
   . createInformation

```
