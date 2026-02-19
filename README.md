# CodebaseObserver

> ⚠️ Under development, not ready for use.

Track codebase health metrics over time.

Current supported metrics:

- Number of files (filterable by extension).

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
3. Build the image: `docker build -t mrras/codebase-observer:0.1.0 .`
4. Now run with `docker compose up`

#### Publish

`docker push mrras/codebase-observer:0.1.0`

### Web

#### Development

`./gradlew :web:wasmJsBrowserDevelopmentExecutableDistribution`

When the server is running, the development build will be accessible at http://localhost:8080/dev/.

### CLI

#### Development

- General help: `./gradlew :cli:run`
- Count files help: `./gradlew :cli:run --args="count-files --help"`
- Count files execute: `./gradlew :cli:run --args="count-files"`
- Count files execute and upload: `./gradlew :cli:run --args="count-files --path . --server http://localhost:8080"`

#### Release

`./gradlew :cli:shadowJar`