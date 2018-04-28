# Introduction

This project is a standalone application with a simple HTTP API using
the libraries commonly used in Dragos Platform microservices.

Before starting, you may want to read up a little on...
1. [Kotlin][https://kotlinlang.org], the Java-interoperable language
used for the production code
1. [RxJava][https://github.com/ReactiveX/RxJava] (JVM implementation of
[ReactiveX][http://reactivex.io/]), a library for composing asynchronous
and event-based programs by using observable sequences
1. [Ratpack][https://ratpack.io/], a lightweight, promise-based embedded
web server
1. [Groovy][http://groovy-lang.org/], the Java-interoperable language
used for the test code
1. [Spock][http://spockframework.org/], a Groovy-based testing and
mocking framework
1. [Gradle][https://gradle.org/], a Groovy-based build tool

# IDE

You are free to use any IDE you'd like, but an up-to-date version of
IntelliJ Community Edition supports Kotlin and Groovy syntax, as well as
Spock and Gradle, out of the box.

# Build

From the root of the repository run `./gradlew clean build` (Mac/Linux)
or `gradlew.bat clean build` (Windows) to build the project. This will
download the correct version of Gradle to a cache in your home directory
and execute the `clean` and `build` tasks. Note that until the bugs in
the project are fixed, the build will fail during the `test` task.

# Assignment Instructions

Perform each task in it's own branch (you can branch off another branch
if desired). Upon finishing each task, submit a pull request using the
BitBucket UI and add [Jon Peterson][jpeterson@dragos.com] and [Joe
Percivall][jpercivall@dragos.com] as reviewers.

### Tasks

1. **Fix a bug where a new customer is being created even when the
   requester has insufficient privileges.**

   Each of the "try to create with..." tests will pass when this is
   fixed.

1. **Fix a bug where new customers are being created with duplicate
   IDs.**

   The "create many" test will pass when this is fixed.

1. **Implement caching to improve performance when getting privileges
   for an auth token.**

   When implemented, the "create many" test should complete in a few
   seconds.

1. **Bonus: Implement an alternate CustomerRepository that persists
   customers data.**

   Can be to flat file(s), a database, etc.

### Hints

If you get stuck for a while on a bug fix and need a point in the right
direction, [Jon Peterson][jpeterson@dragos.com] or [Joe Percivall][
jpercivall@dragos.com] can provide hints.