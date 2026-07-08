# Agenda

1. The Demo Application
2. Your first performance test
3. Making the repo maintainable
4. Integration into the pipeline
5. Creating good performance tests
6. Some solutions to tricky problems

# The Demo Application

- The demo application is a book store that has suddenly gone viral
- Before it has gone viral, no one paid attention to performance
- The browsing for books isn't even paginated and now its very slow
- Running the the BE and Keycloak: `docker compose up -d`
- Running the FE: `npm run dev`

# Your first performance test

- Before we fix the missing pagination, we should write a performance test
  that will validate that we can achieve the required performance after our change
- We take a look at the results
- Explanation of the Gatling Session Object

Running the performance test:
`./gradlew :gatling-tests:gatlingRun --simulation dev.leon.zimmermann.rpt.gatling.scenarios.YourFirstSimulation`


# Making the repo maintainable

- Define Assertions as constants
- Define Load sizes as constants
- These should be defined according to your business requirements
- Use monitoring to determine realistic load sizes
- Let Claude generate a Gradle Task to keep the repo up to date

# Integration into the pipeline

- Debugging: Run everything locally
- Very important requirement: Run them as a quality gate like unit tests
- Important but not very important requirement
  1. Run a replica Application on a Dedicated Server
  2. Run your tests against prod (maybe as a nightly job)

! Always monitor your application during performance tests
! You should be able to start any test against any stage with the press of a button

# Creating good performance tests

## Different kinds of performance tests

- Load tests: Can the system manage a certain load
- Spike tests: Can the system respond to sudden bursts of peak loads
- Stress tests: Handle peak loads that are beyond the limits of anticipated loads
- Scalability tests: How well can the system grow
- Concurrency tests: Does the system still behave correctly with many concurrent users

## Translating kind of performance test into Gatling

- You need monitoring as a prerequisite
- Load profiles
  - Define an expected average load according to your monitoring data
  - Define an expected peak load according to your monitoring data
- Define acceptable response time limits for your application (business consideration)
- Define injection profiles that model your type of performance test

## Injection Profiles

open system (we define arrival frequency of users):

- constantUsersPerSec
- rampUsers
- atOnceUsers
- stressPeakUsers

closed system (we define the number of concurrently active users):

- constantConcurrentUsers
- rampConcurrentUsers

# Some common problems

- Authentication
- Test Data Generation

# Feel free to connect and give feedback 😃

### LinkedIn

![LinkedIn profile Leon Zimmermann](./slides/qr-code-linked-in.png)

https://www.linkedin.com/in/leon-zimmermann-5179831a4/

### The github repository for this talk

![Github Repository](./slides/qr-code-github-repo.png)

https://github.com/LeonZimmermann/reliable-performance-testing

