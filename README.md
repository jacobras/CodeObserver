# CodebaseObserver

> ⚠️ Under development, not ready for use.

Track codebase health metrics over time.

Current supported metrics:

- Lines of code (including comments, imports, blank lines).
- Number of Gradle modules.
- Height of the Gradle dependency tree (the longest path from a module to its root consumer).

## Run locally

### Server

#### Development

```bash
./gradlew :server:run
```

This hosts both the API and the UI (see Web below).

#### Release

1. Build the web app: `./gradlew :web:wasmJsBrowserDistribution`
2. Build the server: `./gradlew :server:shadowJar`
3. Build the image: `docker build -t mrras/codebase-observer:0.3.0 .`
4. Now run with `docker compose up`

#### Publish

`docker push mrras/codebase-observer:0.3.0`

### Web

#### Development

`./gradlew :web:wasmJsBrowserDevelopmentExecutableDistribution`

When the server is running, the development build will be accessible at http://localhost:8080/dev/.

### CLI

#### Development

- General help: `./gradlew :cli:run`
- Measure code help: `./gradlew :cli:run --args="measure-code --help"`
- Measure code execute: `./gradlew :cli:run --args="measure-code"`
- Measure code execute and upload:
  `./gradlew :cli:run --args="measure-code --project test --server http://localhost:8080"`
- Measure Gradle help: `./gradlew :cli:run --args="measure-gradle --help"`
- Measure Gradle execute: `./gradlew :cli:run --args="measure-gradle"`
- Measure Gradle execute and upload:
  `./gradlew :cli:run --args="measure-gradle --project test --server http://localhost:8080"`

#### Release

`./gradlew :cli:shadowJar`