# Synthetic Paper/Folia integration probe

Test-only plugin. Never install it on a production server. It creates a temporary
pig with a real entity scheduler and uses a dynamic proxy as the player passed to
NordTab. This verifies scheduler ownership and cancellation, not a real client's UI.

From the repository root after `mvn verify`, build the probe on PowerShell:

```powershell
mvn dependency:build-classpath -Dmdep.outputFile=target/probe-classpath.txt
$probeClasspath = (Get-Content target/probe-classpath.txt -Raw).Trim() + ';target/classes'
New-Item -ItemType Directory -Path target/probe-classes -Force | Out-Null
javac -encoding UTF-8 -cp $probeClasspath -d target/probe-classes test-support/folia-probe/NordTabFoliaProbe.java
jar --create --file target/NordTabFoliaProbe.jar -C target/probe-classes . -C test-support/folia-probe plugin.yml
```

Install NordTab and this probe only in an isolated synthetic server, bound to
localhost, with no real configurations, accounts, worlds or player data.
Wait for `NORDTAB_PROBE_PASS`; absence of that marker is not success.
`NORDTAB_PROBE_FAIL` or plugin exceptions indicate a failed test.

## Verified 2026-10-07

- Same NordTab 1.1.0 JAR, SHA-256:
  `f688cd6b45e73d584f423c1fb1a3c31e215d1d9b48987ceeb6ea92b0325934fe`.
- Folia 26.2 build 7: entity-owned updates and cancellation after quit passed.
- Paper 26.2 build 132: the same checks passed.
- Console `/nordtab reload` completed on both platforms.
- Three configuration unit tests passed.
- No actual player client or 1000-player load test was performed.
