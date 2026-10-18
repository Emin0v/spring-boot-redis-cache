# Contributing

Use Java 21 and the included Gradle wrapper. Read the README for infrastructure setup and the current verification boundaries.

Before opening a pull request, run:

```bash
./gradlew check
```

Docker must be running: the regression suite starts an isolated Redis container. Add regression coverage for affected catalogue/category reads and invalidation. Explain database/Redis failure ordering and concurrency implications where relevant; the database repository is mocked in the existing tests.

Keep a pull request focused on one problem. Explain the behavior before and after, add a regression test for a behavior change, and state the checks actually run and their limits. Discuss substantial API or architecture changes in an issue first.

For a bug report, include the commit, Java and infrastructure versions, minimal reproduction, expected behavior, actual behavior, and relevant sanitized logs. Report suspected vulnerabilities through [SECURITY.md](SECURITY.md).

