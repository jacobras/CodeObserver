# Contributing

## Running locally

1. Run server: `gradlew :server:run`
2. Build web app: `gradlew :web:clean :web:wasmJsBrowserDevelopmentExecutableDistribution`

When the server is running, the web development build will be accessible at http://localhost:8080/dev/.

Then run CLI commands:

- General help:
    - `gradlew :cli:run`
- Measure code execute:
    - `gradlew :cli:run --args="measure-code --path=.. "`
- Measure code execute and upload:
    - `gradlew :cli:run --args="measure-code --project=test --path=.. --server=http://localhost:8080"`
- Measure Gradle help:
    - `gradlew :cli:run --args="measure-gradle --help"`
- Measure Gradle execute:
    - `gradlew :cli:run --args="measure-gradle --path=.. "`
- Measure Gradle execute and upload:
    - `gradlew :cli:run --args="measure-gradle --project=test --path=.. --server=http://localhost:8080"`
- Report build time:
    - `gradlew :cli:run --args="report-build-time --project=test --name=mainBuild --time=123 --server=http://localhost:8080"`
- Report Detekt:
    - `gradlew :cli:run --args="report-detekt --project=test --htmlFile=../build/reports/detekt/detekt.html --server=http://localhost:8080"`
- Measure artifact size:
    - `gradlew :cli:run --args="measure-artifact-size --project=test --file=app.apk --name=myApp --semVer=1.0.0 --server=http://localhost:8080"`

## Demo web app

`gradlew :web:clean :web:wasmJsBrowserDevelopmentRun -Pdemo=true`

It'll be available at http://localhost:8080/.

## Releasing

Only the maintainer can do this.

### Docker

1. Build the web app: `gradlew :web:wasmJsBrowserDistribution`
2. Build the server: `gradlew :server:shadowJar`
3. Build the image: `docker build -t mrras/code-observer:0.5.0 .`
4. Now run with `docker compose up`
5. Push the image: `docker push mrras/code-observer:0.5.0`

### CLI

`gradlew :cli:shadowJar`

### Web app

Happens in `publish-docs.yml` workflow.