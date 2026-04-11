# Running release builds

Only the maintainer can do this.

## Docker

1. Build the web app: `gradlew :web:wasmJsBrowserDistribution`
2. Build the server: `gradlew :server:shadowJar`
3. Build the image: `docker build -t mrras/code-observer:0.5.0 .`
4. Now run with `docker compose up`
5. Push the image: `docker push mrras/code-observer:0.5.0`

## CLI

`gradlew :cli:shadowJar`

## Web app

Run the `publish-docs.yml` workflow.

## Standalone desktop app

`gradlew :desktop:packageDistributionForCurrentOS`.