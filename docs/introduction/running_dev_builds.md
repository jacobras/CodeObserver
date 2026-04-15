# Running development builds

## Full local build

Recommended for development involving server and web app.

1. Run server: `gradlew :server:run`
2. Build web app: `gradlew :web:clean :web:wasmJsBrowserDevelopmentExecutableDistribution`

When the server is running, the web development build will be accessible at http://localhost:8080/dev/.

Then run CLI commands to feed data:

- General help:
    - `gradlew :cli:run`
- Measure code:
    - `gradlew :cli:run --args="measure --project=test --path=.. --server=http://localhost:8080"`
- Measure artifact size:
    - `gradlew :cli:run --args="measure-artifact-size --project=test --file=app.apk --name=myApp --semVer=1.0.0 --server=http://localhost:8080"`
- Report build time:
    - `gradlew :cli:run --args="report-build-time --project=test --name=mainBuild --time=123 --server=http://localhost:8080"`
- Report Detekt:
    - `gradlew :cli:run --args="report-detekt --project=test --htmlFile=../build/reports/detekt/detekt.html --server=http://localhost:8080"`

## Demo web app

Recommended for development involving only the web app's UI.

`gradlew :web:clean :web:wasmJsBrowserDevelopmentRun -Pdemo=true`

It'll be available at http://localhost:8080/.

## Standalone desktop app

`gradlew :desktop:hotRunJvm`.