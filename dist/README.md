# Prebuilt jars

Committed so the mod can be downloaded directly, without a GitHub login and without waiting for a
CI artifact to be located or before it expires. Build output in version control is unusual; it is a
deliberate convenience here, not a pattern to copy elsewhere in the project.

| File | Built from | SHA-256 |
|---|---|---|
| `nametag-health-1.1.1.jar` | `fix/effects-sync` | `a09d015ab638b76050bd50c45f046cbdb71ec4a889dd4182c6887e5d3ce967fd` |

## This copy can go stale

Nothing enforces that the jar here matches the current source. After changing the mod, refresh it:

```bash
./gradlew build
cp build/libs/nametag-health-*.jar dist/
sha256sum dist/nametag-health-*.jar   # update the table above
```

Or download the `nametag-health-jars` artifact from the latest green run on `main` and copy it here —
that is how this jar was produced, since the build machine and the committing machine differ.
