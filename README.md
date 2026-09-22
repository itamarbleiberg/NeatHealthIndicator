# Nametag Health

A client-side Fabric mod for **Minecraft 1.21.11** that appends a small health indicator to player
nametags, configurable in-game through Mod Menu.

```
Steve ♥ 14          hearts (default)
Steve 14            number
Steve 14/20         fraction
Steve 70%           percent
Steve ███████░░░    bar
```

The colour fades red → yellow → green with remaining health, and absorption shows as a gold `+6`.

This is an original implementation written from scratch. It is inspired by the behaviour of the
well-known *Health Indicators* mod but shares no code with it.

## Download

A prebuilt jar is committed at [`dist/nametag-health-1.0.0.jar`](dist/nametag-health-1.0.0.jar) so it
can be downloaded without a GitHub login:

```
https://github.com/itamarbleiberg/NeatHealthIndicator/raw/main/dist/nametag-health-1.0.0.jar
```

CI also uploads the jar on every run, under the `nametag-health-jars` artifact. See
[`dist/README.md`](dist/README.md) for how to refresh the committed copy after changing the source.

## Requirements

| | |
|---|---|
| Minecraft | 1.21.11 |
| Fabric Loader | 0.18.x or newer |
| Fabric API | required (pulled in by Cloth Config) |
| Cloth Config | **required** — draws the settings screen |
| Mod Menu | optional — only needed to reach the settings screen in-game |

Client-side only. It works on any server, including vanilla ones, and nothing needs to be installed
server-side.

## Building

```bash
./gradlew build
```

The jar lands in `build/libs/`. Use `./gradlew runClient` to launch a dev client.

You need a JDK 21 or newer on your PATH; the build targets Java 21 because that is what Minecraft
1.21.11 requires.

### Confirm these version numbers before your first build

They were written from release notes rather than resolved against a repository, so pin them against
<https://fabricmc.net/develop/> and the mod pages before building. All of them live in
`gradle.properties`:

| Property | Value | Note |
|---|---|---|
| `yarn_mappings` | `1.21.11+build.6` | latest build for 1.21.11 |
| `loader_version` | `0.19.5` | dev-time loader; the mod itself only requires >=0.18.0 |
| `fabric_version` | `0.141.6+1.21.11` | |
| `loom_version` | `1.14-SNAPSHOT` | resolves to 1.14.10, which **requires Gradle 9.2+** |
| `cloth_config_version` | `21.11.153` | |
| `mod_menu_version` | `17.0.0` | |

The Gradle wrapper is pinned to 9.2.0 for that Loom requirement — 8.x fails with a variant
mismatch on `org.gradle.plugin.api-version`. The CI workflow prints the current upstream versions
on every run, so the log is the quickest place to check for drift.

1.21.11 is the last obfuscated Minecraft release, so Yarn mappings still apply. If you later port to
26.1 or newer you will need to move to Mojang's official mappings, since Yarn was retired after
1.21.11.

## Settings

Reachable from Mod Menu, or by editing `config/nametag_health.json` directly. Out-of-range values in
a hand-edited file are clamped on load rather than rejected.

**General** — master toggle, whether to show on players and/or mobs, "only when damaged", and a
maximum distance (4–128 blocks) past which nametags render untouched.

**Appearance** — style, decimal places (0–2), the heart glyph, grey brackets, absorption suffix, and
the bar's length and characters.

**Colour** — gradient / stepped / fixed, the fixed and absorption colours, and an option to colour
only the symbol and leave the number white.

## How it works

The interesting decision is *where* the hook goes. Rather than intercepting the nametag draw call,
`EntityRendererMixin` injects into the tail of `EntityRenderer#updateRenderState` and rewrites
`EntityRenderState.displayName`, which is the field vanilla's `renderLabelIfPresent` reads:

- The render state is built once per entity per frame with the entity still in scope, so health is
  available without any extra lookup.
- It avoids the submit-based render pipeline introduced in 1.21.9 entirely — no matrices, no vertex
  consumers, no `OrderedRenderCommandQueue`. Vanilla draws whatever `Text` it is handed.
- The change is scoped to the in-world label only. Chat, the tab list and death messages keep the
  original name, which is what you want.

Other players' health is read from the `LivingEntity` health tracker, which the server syncs to all
nearby clients. That is the same data vanilla uses to animate hearts, so no server-side support is
needed — though a server running anti-cheat that strips entity data may report stale values.

The only mapping-dependent symbols in the whole mod live in `EntityRendererMixin`:
`EntityRenderer#updateRenderState` and `EntityRenderState.displayName`. Everything else is plain Java
plus stable `Text` APIs. When porting, `tools/dump_mappings.py` prints a class's real member names
straight from the Yarn mappings jar:

```bash
python3 tools/dump_mappings.py 1.21.11+build.6 EntityRenderState EntityRenderer
```

## Layout

```
src/main/java/dev/kiro/nametaghealth/
├── NametagHealth.java              client entrypoint; loads the config
├── config/
│   ├── NametagHealthConfig.java    the options, with clamping
│   ├── ConfigManager.java          Gson load/save
│   ├── DisplayStyle.java
│   └── ColorMode.java
├── render/HealthIndicator.java     builds the indicator Text
├── mixin/EntityRendererMixin.java  the single hook
└── compat/
    ├── ModMenuIntegration.java     Mod Menu entrypoint
    └── NametagHealthConfigScreen.java  Cloth Config screen
tools/make_icon.py                  regenerates the mod icon
```

## Licence

GNU GPL v3.0 — see `LICENSE`, which came with the repository.
