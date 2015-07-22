dibs Contributing
=================

## How to Contribute

1. For any non-trivial contribution:
  1. [Create an issue](https://github.com/dibs-project/dibs/issues) describing the intended
     change [1]
  2. A team member reviews your draft. Adjust it according to the feedback.
2. Create a topic branch
3. Code...
4. Run the code quality checks with `check.sh` and fix possible issues
5. [Create a pull request](https://github.com/dibs-project/dibs/pulls)
6. A team member reviews your contribution. Adjust it according to the feedback. Please do **not**
   rebase or merge after a review, to keep follow-up reviews nice and simple.
7. The contribution is merged \o/

Please make sure to follow the dibs [conventions](doc/developer.md).

[1] A good description contains:

* if the API is modified, any method signature (including a description of possible errors) and
  object signature (including a descripton of properties)
* if the UI is modified, a simple sketch
* if a new dependency is introduced, a short description of the dependency and possible alternatives
  and the reason why it is the best option
