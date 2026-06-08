# CommunityNames for BlueMap Specification

## Status

This document describes the intended behavior and support boundary for BlueMapCommunityNames,
published publicly as CommunityNames for BlueMap.

## Mission

BlueMapCommunityNames is an independent Paper plugin that adds a small Player Roster
overlay to the BlueMap web app. It shows currently online players with zero to three
configured LuckPerms-derived display values.

Core product goals:

- server owners may configure zero to three arbitrary LuckPerms meta keys
- `community_name`, `title`, and `role` are sample defaults only
- zero configured fields means Minecraft-ID-only roster
- missing, null, blank, or sanitized-empty meta values are omitted
- duplicate alias/Minecraft-ID display is suppressed
- BlueMap native player marker names and native player-list DOM remain unchanged

## Runtime Targets

- Paper-compatible server
- Java 25
- Required runtime dependencies installed separately:
  - BlueMap
  - LuckPerms
- Optional runtime dependencies installed separately:
  - Geyser and Floodgate for Bedrock support

nginx or another reverse proxy is not a runtime dependency of this plugin. Webserver,
reverse proxy, firewall, TLS, DNS, and exposure choices are server-administrator
responsibilities.

## Responsibility Boundary

BlueMapCommunityNames is responsible for:

- reading configured LuckPerms meta values
- generating plugin-owned web assets and `players.json`
- registering its own BlueMap web overlay script/style paths
- rendering its own browser roster overlay
- preserving BlueMap native markers
- writing only under `plugins/BlueMapCommunityNames/`

BlueMapCommunityNames is not responsible for:

- BlueMap map rendering, webserver behavior, native marker internals, resource downloads,
  or BlueMap configuration
- LuckPerms permission model, storage, identity targeting, or server-specific meta
  correctness
- Geyser/Floodgate identity behavior beyond values exposed to the plugin runtime
- nginx, reverse proxy, firewall, TLS, DNS, or network exposure choices

## Non-Goals

The plugin must not:

- modify BlueMap native live player marker names
- hide BlueMap native player markers
- replace BlueMap native player markers with custom markers
- rewrite BlueMap's existing player-list DOM
- modify BlueMap, LuckPerms, Floodgate, or Geyser jars or folders
- write under `plugins/BlueMap/`
- write into BlueMap webRoot
- write under `plugins/LuckPerms/`
- write under Floodgate or Geyser folders
- expose UUIDs in JSON or UI by default
- infer Bedrock status from player name prefixes
- use `innerHTML` for player-controlled content
- edit server-side LuckPerms field definitions from the browser UI
- mutate `config.yml` from unauthenticated browser controls

## Ownership

The plugin may only write under its own data folder:

```text
plugins/BlueMapCommunityNames/
```

Generated files live under:

```text
plugins/BlueMapCommunityNames/web/
```

Uninstall is clean when the operator deletes:

- the BlueMapCommunityNames jar
- `plugins/BlueMapCommunityNames/`
- any optional webserver route added for `/bcn/`

No BlueMapCommunityNames-owned files may remain under BlueMap, LuckPerms, Floodgate, or
Geyser folders.

## Web Exposure

BlueMapCommunityNames generates web files under its own plugin folder and registers
`bcn/overlay.js` and `bcn/overlay.css` with BlueMap.

Some server environments may need a same-origin `/bcn/` route so the browser can fetch
the generated files. nginx is one possible way to do that, but it is optional and
environment-specific. Any reverse-proxy example for this plugin is illustrative and does
not imply that nginx is required or that every BlueMap/proxy topology is supported by
this plugin.

If `/bcn/` is not reachable from the browser:

- BlueMap should continue working normally
- the server-side plugin can still generate files
- the browser overlay may fail to load
- `/bcn status` should help diagnose the plugin-owned paths and last JSON write status

No compatibility mode may copy files into BlueMap webRoot.

## Build

- Classic Bukkit/Paper `plugin.yml`
- `depend: [BlueMap, LuckPerms]`
- `softdepend: [floodgate]`
- Java toolchain 25
- Gradle wrapper is included
- Reproducible local build command:

```sh
./gradlew clean test build --no-daemon
```

No BlueMap, LuckPerms, Geyser, or Floodgate jars are bundled into the release artifact.

## BlueMap Integration

Use documented BlueMapAPI methods for lifecycle and web-app registration:

- register one `BlueMapAPI.onEnable(api -> { ... })` listener during plugin enable
- inside that consumer, register:
  - `bcn/overlay.js`
  - `bcn/overlay.css`
- unregister the listener on plugin disable
- never write to `api.getWebApp().getWebRoot()`

Registration must be idempotent across BlueMap reloads.

## Generated Web Assets

On enable, generate:

- `plugins/BlueMapCommunityNames/web/overlay.js`
- `plugins/BlueMapCommunityNames/web/overlay.css`
- `plugins/BlueMapCommunityNames/web/players.json`

Templates are stored under `src/main/resources/web/`. Generated overlay JS/CSS are
deterministic and overwritten on enable and `/bcn reload`.

Initial `players.json` is generated even when no players are online.

## LuckPerms Integration

- Obtain LuckPerms through Bukkit `ServicesManager`.
- Resolve online player meta through the LuckPerms player adapter.
- Default example fields: `community_name` as alias, `title` as chip, and `role` as chip.
- Defaults are examples only; arbitrary keys such as `nickname`, `guild_name`,
  `event_rank`, or other server-defined keys must work.
- Active roster fields are read from `player-roster.luckperms-fields.fields`.
- Explicit empty `player-roster.luckperms-fields.fields` means Minecraft-ID-only roster.

`player-roster.name-display.mode` controls how the alias field affects row names:

- `alias_as_primary`: alias is the main row name when present; Minecraft ID is the
  fallback and may be shown as prefixed subtext.
- `minecraft_id_as_primary`: Minecraft ID is always the main row name; alias may be
  shown as subtext or, if explicitly configured, as a chip.
- `minecraft_id_only`: Minecraft ID is always the main row name; alias is hidden unless
  `show-alias-as-chip` is explicitly enabled.

Alias values that are missing, null, blank, sanitized empty, or equal to the Minecraft
ID after normalization must not create empty or duplicate subtext/chips.

Java and Bedrock/Floodgate identities may be separate LuckPerms users. Depending on the
server's Floodgate/LuckPerms setup, Bedrock players may need UUID-based targeting for
meta assignment. UUIDs must not be exposed in JSON or UI by default.

## JSON Contract

`players.json` uses schema version `2` and includes:

- roster settings needed by the overlay
- zero or more online players
- player display fields derived from configured LuckPerms meta
- `bedrock` detection when Floodgate is available
- no UUIDs by default

Player-controlled strings must be sanitized before publication.

## Browser Overlay

The overlay is a lightweight roster with:

- collapsed/expanded panel behavior
- search
- optional filter chips for configured filterable fields
- details/chips collapse
- browser-local settings such as opacity and language

Browser controls do not edit server config, LuckPerms keys, or player meta.

## Compatibility Notes

The plugin has been validated with Paper 26.1.2, Java 25, BlueMap, LuckPerms, and optional
Geyser/Floodgate in a local test environment. Optional same-origin `/bcn/` routing was
also tested. That is a tested environment note, not a deployment requirement.

## Credits and Attribution

Thanks to the maintainers and contributors of BlueMap, LuckPerms, Geyser, Floodgate, and
Paper, plus the developers, server operators, administrators, and community members
across the Minecraft ecosystem whose work and feedback make community server projects
possible.

BlueMapCommunityNames is independent and is not affiliated with, endorsed by, or
supported by Mojang, Microsoft, Minecraft, BlueMap, LuckPerms, Geyser, Floodgate, or
Paper.
