# drt-lib

`drt-lib` is a shared Scala library for the DRT ecosystem.

It contains common domain models and utilities used by other DRT services, including:

- shared arrival, port, time, auth, and prediction models
- JVM-only database, messaging, notifications, and Keycloak integrations
- protobuf schemas and ScalaPB-generated messages used for actor persistence and serialization

## Project layout

- `shared/` - cross-platform domain code shared by JVM and Scala.js
- `jvm/` - JVM-only integrations such as DAOs, protobuf serialisation, notifications, and auth clients
- `proto/` - protobuf schemas compiled to managed Scala sources during the JVM build

## Requirements

- Java 17
- sbt

## Running tests

Run the standard local verification script from the repository root:

```bash
./run_tests.sh
```

This runs formatting, compile, test, coverage, and dependency update checks.

Useful direct sbt commands (run all tests across JVM and JS with error-only output, run a specific JVM test suite, or run the full coverage and dependency update flow):

```bash
sbt --error +test
sbt "crossJVM / Test / testOnly uk.gov.homeoffice.drt.db.dao.ShiftStaffRollingDaoSpec"
sbt clean scalafmtAll scalafmtSbt compile coverage test coverageOff coverageReport dependencyUpdates
```

To publish locally for testing in other projects, use:

```bash
sbt +publishLocal
```

## Notes

- The build is a cross-project with `crossJVM` and `crossJS` modules.
- Protobuf generation is wired through `sbt-protoc` and ScalaPB for the JVM build.
