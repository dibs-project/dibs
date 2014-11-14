HUB-User Documentation
======================

Overview
---------

Structure:

```
  .--------------------. .----------. .-------------------.
  | ApplicationService | | Settings | | User              |
  |--------------------| '----------' |-------------------|
  | register           |              | createInformation |
  | createUser         |              '-------------------'
  | createCourse       |
  '--------------------'

  .-------------. .-------------. .------------.
  | Information | | Application | | Evaluation |
  '-------------' '-------------' '------------'

  .----------------------. .----------------. .---------------------. .-----------.
  | Course               | | AllocationRule | | Quota               | | Criterion |
  |----------------------| |----------------| |---------------------| |-----------|
  | createAllocationRule | | createQuota    | | addRankingCriterion | |evaluate   |
  | apply                | '----------------' | generateRanking     | '-----------'
  | generateRankings     |                    '---------------------'
  | publish              |
  | unpublish            |
  '----------------------'                                                                 
 

  .---------.
  | Journal |
  '---------'
```

Process:

```
   . Applicant                            | Employee                        | System
  ----------------------------------------+---------------------------------+-----------------------
   .                                      | ApplicationService.createUser   |
   .                                      | ApplicationService.createCourse |
   .                                      | Course.createAllocationRule     |
   .                                      | AllocationRule.createQuota      |
   .                                      | Quota.addRankingCriterion       |
   .                                      | Course.publish                  |
   . ApplicationService.register          |                                 |
   . User.createInformation               |                                 |
   . Course.apply                         |                                 |
   .                                      |                                 | Course.generateRankings

```
