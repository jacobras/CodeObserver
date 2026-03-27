# CodebaseObserver

> ⚠️ Under development, not ready for use.

Track codebase health metrics over time.

Current supported metrics:

- Lines of code (including comments, imports, blank lines).
- Number of Gradle modules.
- Height of the Gradle dependency tree (the longest path from a module to its root consumer).

## Run locally

1. Run server: `./gradlew :server:run`
2. Build web app: `./gradlew :web:clean :web:wasmJsBrowserDevelopmentExecutableDistribution`

When the server is running, the web development build will be accessible at http://localhost:8080/dev/.

Then run CLI commands:

- General help: `./gradlew :cli:run`
- Measure code help: `./gradlew :cli:run --args="measure-code --help"`
- Measure code execute: `./gradlew :cli:run --args="measure-code --path=.. "`
- Measure code execute and upload:
  `./gradlew :cli:run --args="measure-code --project=test --path=.. --server=http://localhost:8080"`
- Measure Gradle help: `./gradlew :cli:run --args="measure-gradle --help"`
- Measure Gradle execute: `./gradlew :cli:run --args="measure-gradle --path=.. "`
- Measure Gradle execute and upload:
  `./gradlew :cli:run --args="measure-gradle --project=test --path=.. --server=http://localhost:8080"`
- Report build time:
  `./gradlew :cli:run --args="report-build-time --project=test --name=mainBuild --time=123 --server=http://localhost:8080"`
- Report Detekt:
  `./gradlew :cli:run --args="report-detekt --project=test --htmlFile=../build/reports/detekt/detekt.html --server=http://localhost:8080"`

## Release

### Docker

1. Build the web app: `./gradlew :web:wasmJsBrowserDistribution`
2. Build the server: `./gradlew :server:shadowJar`
3. Build the image: `docker build -t mrras/codebase-observer:0.4.0 .`
4. Now run with `docker compose up`
5. Push the image: `docker push mrras/codebase-observer:0.4.0`

### CLI

`./gradlew :cli:shadowJar`