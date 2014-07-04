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
  '-------------'  | evaluate  |
                   '-----------'
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
