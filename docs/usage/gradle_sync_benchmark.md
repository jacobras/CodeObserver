# Gradle sync benchmark

Inspired by <https://www.youtube.com/watch?v=O51j-RO_GQM>, this workflow runs Gradle Profiler to benchmark the time it
takes to sync the project every night at 3 AM and feeds it into CodeObserver.

**.github/workflows/benchmark-gradle-sync**

```yaml
name: Benchmark Gradle sync

on:
    schedule:
        -   cron: '0 3 * * *'
    workflow_dispatch:

jobs:
    benchmark:
        runs-on: ubuntu-latest
        steps:
            -   name: Checkout
                uses: actions/checkout@v6

            -   name: Build setup
                uses: ./.github/actions/build-setup

            -   name: Install Gradle Profiler using SDKMAN
                run: |
                    curl -s "https://get.sdkman.io" | bash
                    source "$HOME/.sdkman/bin/sdkman-init.sh"
                    sdk install gradleprofiler 0.24.0
                    echo "$HOME/.sdkman/candidates/gradleprofiler/current/bin" >> $GITHUB_PATH

            -   name: Run Gradle Profiler
                run: |
                    mkdir -p profiler-results
                    gradle-profiler \
                      --gradle-user-home "$HOME/.gradle" \
                      --benchmark \
                      --output-dir profiler-results \
                      --scenario-file .github/scenarios/sync.scenarios \
                      --warmups 4 \
                      --iterations 3

            -   name: Upload results
                uses: actions/upload-artifact@v7
                with:
                    name: profiler-results
                    path: profiler-results

            -   name: Compute median build time
                id: median
                run: |
                    median_s=$(kotlin .github/scripts/get_gradle_profiler_median_seconds.main.kts)
                    echo "Median build time: ${median_s} s"
                    echo "median_s=${median_s}" >> "$GITHUB_OUTPUT"
                    echo "## 📊 Gradle Profiler Results" >> "$GITHUB_STEP_SUMMARY"
                    echo "**Median build time:** ${median_s} s" >> "$GITHUB_STEP_SUMMARY"

            -   name: CodeObserver
                uses: jacobras/CodeObserver@v0
                timeout-minutes: 5
                with:
                    command: report-build-time --name gradle-sync --time ${{ steps.median.outputs.median_s }}
                    server: ${{ secrets.CODEOBSERVER_SERVER_URL }}
                    project: your-project-id
```

**.github/scripts/get_gradle_profiler_median_seconds.main.kts**

```kotlin
#!/usr/bin/env kotlin

import java.io.File
import kotlin.math.roundToLong
import kotlin.system.exitProcess

val csv = File("profiler-results/benchmark.csv")

if (!csv.isFile) {
    System.err.println("benchmark.csv not found in profiler-results/")
    exitProcess(1)
}

val measuredValues: List<Double> = csv.readLines()
    .map { it.split(",").map(String::trim) }
    .filter { it.firstOrNull()?.startsWith("measured build") == true }
    .mapNotNull { it.getOrNull(1)?.toDoubleOrNull() }

if (measuredValues.isEmpty()) {
    System.err.println("No measured builds found in benchmark.csv")
    exitProcess(1)
}

val sorted = measuredValues.sorted()
val mid = sorted.size / 2
val medianMs = if (sorted.size % 2 == 1) {
    sorted[mid]
} else {
    (sorted[mid - 1] + sorted[mid]) / 2.0
}

val medianSeconds = (medianMs / 1000.0).roundToLong()
println(medianSeconds)
```

**.github/scenarios/sync.scenarios**

```kotlin
default - scenarios = ["build_dry_run"]
build_dry_run {
    title = "Build dry run"
    tasks = ["build"]
    gradle - args = ["--dry-run"]
    clear - configuration - cache - state - before = BUILD
}
```