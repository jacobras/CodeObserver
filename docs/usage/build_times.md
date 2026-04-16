# Build times

This will record the passed in build time. The results will be shown in a chart over time:

![](../images/build_times.png)

## With the CLI tool

Run the `report-build-time` command with the following arguments:

| Argument    | Required? | Description                    |
|-------------|-----------|--------------------------------|
| `--server`  | ✅         | URL of the CodeObserver server |
| `--project` | ✅         | Name of the project            |
| `--name`    | ✅         | Name of the build              |
| `--time`    | ✅         | Build time in seconds          |

The hash and date of the last Git commit will be used to store the results. To backfill older results, check out
an older commit and run the command again.

## With the GitHub Action

```yaml
  -   name: CodeObserver
      uses: jacobras/CodeObserver@v0
      timeout-minutes: 5
      with:
          command: report-build-time --name clean-build --time 123
          server: ${{ secrets.CODEOBSERVER_SERVER_URL }}
          project: your-project-id
```

The hash and date of the last Git commit will be used to store the results.