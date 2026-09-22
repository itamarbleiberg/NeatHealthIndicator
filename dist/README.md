# Prebuilt jars

Committed so the mod can be downloaded directly, without a GitHub login and without waiting for a
CI artifact to be located or before it expires. Build output in version control is unusual; it is a
deliberate convenience here, not a pattern to copy elsewhere in the project.

| File | Built from | SHA-256 |
|---|---|---|
| `nametag-health-1.0.0.jar` | `main` @ `ebfdc15` | `62d700bd6583666b4fa905db92528752598c15c1e4029ca0dec29318c6e955bc` |

## This copy can go stale

Nothing enforces that the jar here matches the current source. After changing the mod, refresh it:

```bash
./gradlew build
cp build/libs/nametag-health-*.jar dist/
sha256sum dist/nametag-health-*.jar   # update the table above
```

Or download the `nametag-health-jars` artifact from the latest green run on `main` and copy it here —
that is how this jar was produced, since the build machine and the committing machine differ.
