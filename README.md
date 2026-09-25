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

A prebuilt jar is committed at [`dist/nametag-health-1.1.1.jar`](dist/nametag-health-1.1.1.jar) so it
can be downloaded without a GitHub login:

```
https://github.com/itamarbleiberg/NeatHealthIndicator/raw/main/dist/nametag-health-1.1.1.jar
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

**General** — master toggle, players and/or mobs, "only when damaged", maximum distance (4–128
blocks), and the keybind-driven visible state.

**Visibility** — *reveal mode* (always, while a key is held, only the entity under your crosshair, or
only while holding a listed item); *require line of sight*, which hides the indicator behind walls
even though vanilla draws nametags through them; *hide with the HUD* on F1; *skip invisible
entities*; a *team filter* for allies or opponents only; an entity id *allow/deny list*; and a
*max-health range* so you can ignore chickens or leave bosses to their own bar.

**Content** — *placement* before, after or instead of the name; *armour points*; *status effect*
markers as dots or a count (see the limitation below); player *ping*; *recent change* (`-4` / `+2`)
held for a configurable window, with rapid hits accumulating into one running total; and
*abbreviation* of large numbers (`1.2k`) for high-health modded mobs.

**Colour** — gradient / stepped / fixed; three *palettes* including a colour-blind safe orange→blue
ramp and a brightness-only monochrome one; and *configurable thresholds* which set both where the
stepped colours change and where the gradient stops blending, so widening the band softens the fade.
Plus a *low-health pulse* that fades the colour in and out below a threshold.

**Text style** — bold, italic, underline.

**Advanced** — *smoothing*, which slides the displayed value toward the real one instead of snapping,
and a *rebuild interval* that throttles how often the text is regenerated.

### The format template

The single most flexible option. Leave it empty and the style plus the toggles above decide the
layout; set it and you control the whole thing:

| Token | Renders |
|---|---|
| `{sym}` `{hp}` `{max}` | heart glyph, current health, maximum |
| `{frac}` `{pct}` `{bar}` | `14/20`, `70%`, `███████░░░` |
| `{abs}` `{armor}` | absorption `+6`, armour `◆8` |
| `{fx}` `{ping}` `{delta}` | effect markers, `42ms`, `-4` |

The template is split on spaces and each word rendered on its own. **A word whose tokens all resolve
to nothing is dropped entirely**, which is what stops optional pieces from leaving gaps behind — so
`{hp} {abs} {ping}` collapses cleanly to just the health when there is no absorption and no ping.
Unknown tokens are left visible rather than swallowed, so typos are obvious.

```
{sym} {hp}/{max} {delta}     →  ♥ 14/20 -4
HP:{pct} {fx}                →  HP:70% RP
{bar} {ping}                 →  ███████░░░ 42ms
```

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

### Why status effects are only a count

Health is synced; status effects are not. `LivingEntity.activeStatusEffects` is a plain server-side
map, and effect packets are sent to the affected player alone — `sendEffectToControllingPlayer` is
named for exactly that. So `getStatusEffects()` returns an empty collection for anyone else's entity,
and no client-only mod can tell you a nearby player has Strength II.

What *is* synced is `POTION_SWIRLS`, the tracked particle list that draws the visible swirls. The
effect marker counts those, via a mixin accessor since the field is private. Consequences worth
knowing:

- You get **how many** effects, never **which**. Naming them needs a server-side mod.
- Effects hidden from particles — ambient beacon effects, or anything applied with `hideParticles` —
  contribute nothing, matching what you see in world.

Rebuilding the text touches the registries and allocates a small tree, so it is throttled by the
rebuild interval and cached per entity. Anything mid-animation — smoothing, pulsing, or a delta still
on screen — opts out of the cache, since a stale frame would defeat the point. The line-of-sight
raycast is the last check in the visibility gate, so it only runs for entities that passed everything
cheaper.

Per-entity history (for smoothing and deltas) lives in a weakly-keyed map, so an entity leaving render
distance takes its sample with it and nothing needs pruning on a timer.

The only mapping-dependent symbols in the whole mod live in `EntityRendererMixin`:
`EntityRenderer#updateRenderState` and `EntityRenderState.displayName`. When porting,
`tools/dump_mappings.py` prints a class's real member names straight from the Yarn mappings jar:

```bash
python3 tools/dump_mappings.py 1.21.11+build.6 EntityRenderState EntityRenderer
```

Both keybinds are registered **unbound** — bind them under Controls → Nametag Health. Claiming a
default risks stomping on something the player already uses.

## Layout

```
src/main/java/dev/kiro/nametaghealth/
├── NametagHealth.java                  client entrypoint; config + keybinds
├── config/
│   ├── NametagHealthConfig.java        every option, with clamping
│   ├── ConfigManager.java              Gson load/save
│   ├── OptionLabel.java                lets the screen label any enum dropdown
│   └── DisplayStyle / ColorMode / Palette / Placement
│       / RevealMode / FilterMode / TeamFilter / EffectStyle
├── render/
│   ├── HealthIndicator.java            orchestrates; placement and caching
│   ├── Visibility.java                 every "should this show" rule
│   ├── IndicatorFormatter.java         the token template engine
│   ├── HealthSamples.java              per-entity history for smoothing + deltas
│   ├── ColorPalettes.java              gradient, steps, pulse
│   └── StatusEffectGlyphs.java         effect letters and vanilla particle colours
├── input/Keybinds.java                 toggle and hold-to-reveal
├── mixin/EntityRendererMixin.java      the single hook
└── compat/
    ├── ModMenuIntegration.java         Mod Menu entrypoint
    └── NametagHealthConfigScreen.java  Cloth Config screen
tools/
├── check_lang.py                       fails CI on a missing translation key
├── dump_mappings.py                    member names from the Yarn mappings jar
└── make_icon.py                        regenerates the mod icon
```

## Licence

GNU GPL v3.0 — see `LICENSE`, which came with the repository.
