# BlueMapCommunityNames Specification

## Status

This document is the source of truth for implementation.

BlueMapAPI was checked before implementation:

- Official repository: https://github.com/BlueMap-Minecraft/BlueMapAPI
- Latest GitHub release observed on 2026-06-01: `v2.7.8`, released 2026-04-07
- Official repository build files checked:
  - `build.gradle.kts`
  - `settings.gradle.kts`
  - `gradle/libs.versions.toml`
- Current API surface checked:
  - `de.bluecolored.bluemap.api.BlueMapAPI`
  - `de.bluecolored.bluemap.api.WebApp`
- Official wiki checked for dependency instructions.

The official wiki currently documents:

```kotlin
repositories {
    maven("https://repo.bluecolored.de/releases")
}

dependencies {
    compileOnly("de.bluecolored:bluemap-api:2.7.7")
}
```

GitHub releases show `v2.7.8` as latest, while the wiki dependency snippet still shows
`2.7.7`. The build will prefer `de.bluecolored:bluemap-api:2.7.8` from the official
BlueColored release repository. If resolution fails, the supported fallback is placing
`libs/BlueMapAPI-2.7.8.jar` or another matching `libs/BlueMapAPI-*.jar` in the project
and using it as a local `compileOnly` dependency.

## Mission

Create a private PaperMC plugin named `BlueMapCommunityNames`.

The plugin adds a small independent Player Roster overlay to the BlueMap WebApp showing
currently online players with optional LuckPerms-derived aliases and chips.

- Server owners may configure zero to three arbitrary LuckPerms meta keys.
- `community_name`, `title`, and `role` are default examples only.
- If zero fields are configured, display Minecraft IDs only.
- Missing, null, blank, or sanitized-empty meta values are omitted.
- BlueMap native player marker names and native player-list DOM remain unchanged.

The plugin is intended for private/internal community servers. README and config text
may be English.

## Runtime Targets

- Server: PaperMC `26.1.2+`
- Java: Java 25
- Required runtime dependencies:
  - BlueMap
  - LuckPerms
- Optional runtime dependency:
  - Floodgate
- Supported public deployment:
  - nginx exposes `/bcn/` to the plugin-owned web folder.

## Non-Goals

The plugin must not:

- Modify BlueMap native live player marker names.
- Hide BlueMap native player markers.
- Replace BlueMap native player markers with custom markers.
- Rewrite BlueMap's existing player-list DOM.
- Modify BlueMap, LuckPerms, Floodgate, or Geyser jars or folders.
- Write under `plugins/BlueMap/`.
- Write into BlueMap webRoot.
- Write under `plugins/LuckPerms/`.
- Write under Floodgate or Geyser folders.
- Use nginx to rewrite BlueMap live JSON.
- Expose UUIDs in JSON or UI by default.
- Infer Bedrock status from player name prefixes.
- Use `innerHTML` for player-controlled content.
- Edit server-side LuckPerms field definitions from the browser UI.
- Mutate `config.yml` from unauthenticated browser controls.

## Ownership

The plugin may only write under its own data folder:

```text
plugins/BlueMapCommunityNames/
```

Generated files must live under:

```text
plugins/BlueMapCommunityNames/web/
```

Uninstall is clean when the operator deletes:

- `plugins/BlueMapCommunityNames.jar`
- `plugins/BlueMapCommunityNames/`
- the nginx `/bcn/` alias, if installed

No files may remain under BlueMap, LuckPerms, Floodgate, or Geyser folders.

## Deployment

Strict-clean mode requires nginx. nginx must expose:

```text
/bcn/ -> plugins/BlueMapCommunityNames/web/
```

BlueMap integrated webserver-only mode is not supported in strict-clean mode because the
plugin will not copy files into BlueMap's webRoot.

If nginx is absent or `/bcn/` is misconfigured:

- BlueMap must continue working normally.
- The overlay may fail to load.
- `/bcn status` must report that the public `/bcn/` path may be unreachable.

No compatibility mode may copy files into BlueMap webRoot.

## Repository Layout

```text
BlueMapCommunityNames/
  SPEC.md
  README.md
  build.gradle.kts
  settings.gradle.kts
  src/main/java/...
  src/main/resources/
    plugin.yml
    config.yml
    web/
      overlay.js.template
      overlay.css.template
  src/test/java/...
```

## Build

- Classic Bukkit/Paper `plugin.yml`.
- `api-version: "26.1.2"` if accepted by Paper's plugin descriptor parser for target API.
- `depend: [BlueMap, LuckPerms]`.
- `softdepend: [floodgate]`.
  - Floodgate's current upstream Spigot descriptor templates the plugin name. `floodgate`
    is the commonly observed runtime plugin name and must be confirmed against the
    installed jar/plugin list during smoke testing.
- Java toolchain 25.
- Gradle wrapper is included and pins Gradle 9.5.0.
- Reproducible local build command:

```sh
./gradlew clean test build --no-daemon
```

- No reobf, MCP mappings, or Spigot remapping.
- `compileOnly` dependencies:
  - `io.papermc.paper:paper-api:26.1.2.build.64-stable` or a newer stable `26.1.2` build.
  - `de.bluecolored:bluemap-api:2.7.8`, with local jar fallback documented above.
  - LuckPerms API 5.x stable.
  - Floodgate API optional.
- Unit tests use JUnit Jupiter.

## BlueMap Integration

Use only documented BlueMapAPI methods checked from `BlueMapAPI` and `WebApp`:

- `BlueMapAPI.onEnable(Consumer<BlueMapAPI>)`
- `BlueMapAPI.unregisterListener(Consumer<BlueMapAPI>)`
- `BlueMapAPI.getInstance()`
- `BlueMapAPI#getBlueMapVersion()`
- `BlueMapAPI#getAPIVersion()`
- `BlueMapAPI#getWebApp()`
- `WebApp#registerScript(String)`
- `WebApp#registerStyle(String)`
- `WebApp#getWebRoot()` for diagnostics only

Registration rules:

- Register one `BlueMapAPI.onEnable(api -> { ... })` listener during plugin enable.
- Inside that consumer only, call:
  - `api.getWebApp().registerScript("bcn/overlay.js")`
  - `api.getWebApp().registerStyle("bcn/overlay.css")`
- Do not call `registerScript` or `registerStyle` anywhere else.
- The consumer may be called multiple times across BlueMap reloads and may run off the
  main server thread.
- Registration must be idempotent for plugin state.
- On plugin disable, unregister the listener with `BlueMapAPI.unregisterListener(...)`.
- Never write to `api.getWebApp().getWebRoot()`.

## Generated Web Assets

On enable, generate:

- `plugins/BlueMapCommunityNames/web/overlay.js`
- `plugins/BlueMapCommunityNames/web/overlay.css`
- `plugins/BlueMapCommunityNames/web/players.json`

Templates are stored in plugin resources under `src/main/resources/web/`.
Generated overlay JS/CSS are deterministic and overwritten on enable and `/bcn reload`;
this is documented because the resource templates are the source of truth.

Initial `players.json` is generated even when no players are online.

## nginx Documentation

README must include:

```nginx
location ^~ /bcn/ {
    alias /path/to/server/plugins/BlueMapCommunityNames/web/;
    add_header Cache-Control "no-store, no-cache, must-revalidate, max-age=0" always;
    expires off;
    try_files $uri =404;
}
```

README must state existing BlueMap `/maps/*/live/*` proxy settings remain unchanged and
must include troubleshooting commands:

```sh
curl -I https://your-map-domain.example/bcn/overlay.js
curl -I https://your-map-domain.example/bcn/players.json
```

A 404 on `/bcn/overlay.js` means the nginx alias is missing or wrong.

## LuckPerms Integration

- Obtain LuckPerms through Bukkit `ServicesManager`.
- Resolve online player meta with:

```java
luckPerms.getPlayerAdapter(Player.class).getMetaData(player).getMetaValue(metaKey)
```

- Default example fields: `community_name` as alias, `title` as chip, and `role` as chip.
- These defaults are examples only; any LuckPerms meta keys such as `nickname`, `guild`,
  `department`, `event_rank`, or `class_name` must work.
- Active roster fields are read from `player-roster.luckperms-fields.fields`.
- Explicit empty `player-roster.luckperms-fields.fields` means Minecraft-ID-only roster.
- If roster fields are missing but old v0.2 `display.fields` exists, adapt those fields.
- If roster fields and `display.fields` are missing but old v0.1 `meta-key` exists,
  convert `meta-key` into one alias field.
- If no roster/display/meta-key config exists, use the default example fields.
- Blank keys are skipped with a warning, duplicate keys are deduped with a warning, and
  more than three enabled fields are truncated with a warning.
- Blank labels fall back to the key.
- If configured fields exist but no valid fields remain, warn and use Minecraft-ID-only
  roster. Do not force `community_name`.
- `/bcn reload` reloads the active display fields.
- `/bcn status` reports zero to three active field keys, labels, display role,
  searchability, filterability, fallback state, and config warnings.
- Subscribe to LuckPerms `UserDataRecalculateEvent`.
- If the affected user is online, schedule a rebuild.
- Do not rely only on events; perform a full refresh every 10 seconds.
- Use LuckPerms API 5.x stable documented APIs only.

## Threading

Never access Bukkit player state asynchronously.

Main thread:

- Read online players.
- Read `Player#getName()`.
- Read player UUID for optional Floodgate lookup.
- Read LuckPerms online-player meta.
- Build immutable DTO snapshots.

Async writer:

- Serialize JSON.
- Write temp file in the same directory.
- Move temp file into place.

Use:

- Debounce: 300 ms for join, quit, and meta-change bursts.
- Single-writer executor to avoid overlapping JSON writes.
- Clean task cancellation and executor shutdown on disable.

## Refresh Triggers

- `PlayerJoinEvent`
- `PlayerQuitEvent`
- LuckPerms `UserDataRecalculateEvent`
- `/bcn rebuild`
- `/bcn reload`
- Periodic full refresh every 10 seconds

## Floodgate

Floodgate is optional.

- If Floodgate API is available and usable, top-level JSON has
  `bedrockDetection: "floodgate"`.
- If unavailable, top-level JSON has `bedrockDetection: "unavailable"`.
- Per player:
  - `bedrock: true` if Floodgate confirms Bedrock.
  - `bedrock: false` if Floodgate is available and says not Bedrock.
  - `bedrock: null` if detection is unavailable.
- Initial UI does not show `[Bedrock]`.
- Never infer Bedrock from prefixes such as `.`.

## Sanitization

Apply this pipeline to every configured LuckPerms meta value:

1. null -> null
2. trim
3. Unicode normalize NFC
4. remove legacy section-sign color/formatting codes
5. remove CR, LF, TAB, and ISO control characters
6. remove bidi control characters:
   - U+061C
   - U+200E
   - U+200F
   - U+202A..U+202E
   - U+2066..U+2069
7. truncate by Unicode code points to max 24
8. optionally add ellipsis when truncated
9. if blank after sanitize, treat as null

Java must not HTML-escape by hand as a substitute for DOM safety.
The web UI must use `textContent` for dynamic values and never `innerHTML`.

## JSON Schema

Emit UTF-8 JSON at:

```text
plugins/BlueMapCommunityNames/web/players.json
```

Schema:

```json
{
  "schemaVersion": 2,
  "generatedAt": "2026-06-01T12:34:56Z",
  "pluginVersion": "0.2.0",
  "bedrockDetection": "floodgate",
  "display": {
    "separator": "/",
    "fields": [
      {"id": "community_name", "key": "community_name", "label": "よび名", "display": "alias", "searchable": true, "filterable": true},
      {"id": "title", "key": "title", "label": "称号", "display": "chip", "searchable": false, "filterable": true}
    ]
  },
  "roster": {
    "nameDisplayMode": "community_name_as_primary",
    "showMinecraftIdAsSubtext": true,
    "minecraftIdPrefix": "@",
    "fields": [
      {"id": "community_name", "key": "community_name", "label": "よび名", "display": "alias", "searchable": true, "filterable": true}
    ]
  },
  "players": [
    {
      "playerName": "<JAVA_PLAYER_NAME>",
      "minecraftId": "<JAVA_PLAYER_NAME>",
      "displayName": "<JAVA_COMMUNITY_NAME>",
      "subName": "@<JAVA_PLAYER_NAME>",
      "display": "<JAVA_COMMUNITY_NAME>",
      "bedrock": false,
      "chips": [
        {"id": "title", "key": "title", "label": "称号", "display": "chip", "searchable": false, "filterable": true, "value": "<JAVA_TITLE>"}
      ],
      "metaValues": [
        {"id": "community_name", "key": "community_name", "label": "よび名", "display": "alias", "searchable": true, "filterable": true, "value": "<JAVA_COMMUNITY_NAME>"},
        {"id": "title", "key": "title", "label": "称号", "display": "chip", "searchable": false, "filterable": true, "value": "<JAVA_TITLE>"}
      ],
      "filterText": "<JAVA_PLAYER_NAME> <JAVA_COMMUNITY_NAME> <JAVA_TITLE>"
    }
  ]
}
```

Rules:

- `schemaVersion` is number `2`.
- `generatedAt` is ISO-8601 UTC.
- `bedrockDetection` is `"floodgate"` or `"unavailable"`.
- `bedrock` is `true`, `false`, or `null`.
- `display.fields` lists configured fields visible to the overlay.
- `roster.fields` lists configured roster fields visible to the overlay.
- `displayName`, `subName`, and `chips` support roster rendering.
- `metaValues` contains only present sanitized values.
- Missing, null, blank, and sanitized-empty values are omitted.
- `display` is final UI string.
- `filterText` supports client-side search.
- No UUID by default.
- Sort players deterministically by case-insensitive `playerName`.
- The v0.2 overlay must tolerate v1 JSON by rendering `player.display || player.playerName`
  and treating missing `display.fields` and `metaValues` as empty.

## Atomic JSON Write

- Write to a temp file in the same directory.
- Move to `players.json`.
- Try `StandardCopyOption.ATOMIC_MOVE`.
- If unsupported, fallback to `REPLACE_EXISTING`.
- Log the ATOMIC_MOVE fallback warning once.
- Store last successful write timestamp for `/bcn status`.
- If write fails, keep previous `players.json` if possible and expose error summary in
  `/bcn status`.

## Commands

All commands require `bluemapcommunitynames.admin`.

`/bcn reload`:

- Reload config.
- Regenerate deterministic overlay assets.
- Rebuild `players.json`.
- If public path or script/style URL changed, warn that `/bluemap reload` may be needed
  because script/style registration happens during BlueMap onEnable.

`/bcn rebuild`:

- Immediately rebuild `players.json`.

`/bcn status`:

- BlueMap detected yes/no.
- BlueMap version/API version if available.
- LuckPerms detected yes/no.
- Floodgate detected yes/no.
- Bedrock detection mode: `floodgate`/`unavailable`.
- Active roster fields with key, label, display role, searchable, and filterable.
- Valid roster field count from 0 to 3 and config fallback state.
- Explicit no-field state, if no LuckPerms roster fields are configured.
- Current online value count per roster field, without dumping player names.
- Config warning count and warning text.
- Plugin data folder.
- Web assets folder.
- Public base path `/bcn/`.
- Last successful JSON write.
- Last error summary.
- Strict-clean mode enabled.
- Warning that nginx alias is required.
- Suggested curl checks.

## Default Config

```yaml
meta-key: "community_name"

player-roster:
  enabled: true
  panel:
    default-state: collapsed
    position: top-right
    max-height: 520px
    max-height-vh: 70
  name-display:
    mode: community_name_as_primary
    show-minecraft-id-as-subtext: true
    minecraft-id-prefix: "@"
  search:
    enabled: true
    targets:
      - minecraft_id
      - alias
  details:
    default-state: expanded
    allow-toggle: true
  filters:
    enabled: true
    collapsed-by-default: true
    field-sections-collapsed-by-default: true
    max-visible-values-per-field: 8
    high-cardinality-threshold: 12
  luckperms-fields:
    max-fields: 3
    fields:
      - id: community_name
        meta-key: community_name
        label: "よび名"
        display: alias
        searchable: true
        filterable: true
      - id: title
        meta-key: title
        label: "称号"
        display: chip
        searchable: false
        filterable: true
      - id: role
        meta-key: role
        label: "ロール"
        display: chip
        searchable: false
        filterable: true

web:
  public-base-path: "/bcn/"
  local-web-dir: "web"
  script-file: "overlay.js"
  style-file: "overlay.css"
  json-file: "players.json"
  panel-title: "Online Players"
  poll-interval-seconds: 5

sanitize:
  trim: true
  strip-legacy-section-codes: true
  remove-control-chars: true
  remove-bidi-controls: true
  normalize: "NFC"
  max-code-points: 24
  ellipsis-on-truncate: true

updates:
  debounce-millis: 300
  periodic-full-refresh-seconds: 10

floodgate:
  enabled: true

logging:
  debug: false
```

## Web Overlay

- `overlay.js` fetches `/bcn/players.json` with `cache: "no-store"`.
- It adds an independent panel to `document.body`.
- It does not query or replace BlueMap's existing player list entries.
- It tolerates JSON fetch failure.
- If JSON is unavailable, show `Community names unavailable`.
- Dynamic values are inserted with `textContent`.
- It supports collapsed roster bar, expand/collapse, drag, reset position, localStorage
  persistence, text search, chip filters, clear filters, and visible/total counts.
- It supports a global details toggle that collapses or expands player-row LuckPerms
  chips while keeping player names and Minecraft ID subtext visible.
- Rows with hidden chips show a subtle hidden-details count.
- Browser appearance settings are local-only: opacity, density, and UI language are
  stored in browser localStorage and applied through CSS variables.
- Browser controls must not edit `config.yml`, change LuckPerms meta keys, or send
  server admin mutations.
- There is no fullscreen maximize.
- Filter options come from current online JSON values and omit missing values.
- Filter field sections are individually collapsible.
- Values are sorted by current online count descending, then by value.
- Only `max-visible-values-per-field` values are shown initially.
- Fields above `high-cardinality-threshold` show an in-section value search.
- If no fields are configured, hide or disable filter controls.
- Multiple filters combine with AND; search combines with filters using AND.
- CSS uses unique `bm-roster*`, `bm-player*`, and `bm-chip*` classes.
- Poll interval comes from generated JS/config, default 5 seconds.

## README

README must explain:

- The plugin adds a BlueMap WebApp roster showing zero to three arbitrary LuckPerms
  display fields.
- `community_name`, `title`, and `role` are examples only.
- It does not change BlueMap marker names.
- It does not modify BlueMap or LuckPerms folders.
- nginx is required for supported strict-clean deployment.
- nginx alias example.
- Setup steps.
- LuckPerms meta example:

```text
/lp user <JAVA_PLAYER_NAME> meta set community_name <JAVA_COMMUNITY_NAME>
```

- Uninstall steps:
  1. stop server
  2. delete `plugins/BlueMapCommunityNames.jar`
  3. delete `plugins/BlueMapCommunityNames/`
  4. remove nginx `/bcn/` alias
  5. reload nginx
  6. start server
- Troubleshooting:
  - overlay missing
  - stale names
  - BlueMap reload behavior
  - Floodgate unavailable
  - nginx path errors

## Unit Tests

Sanitizer:

- null
- blank
- Japanese text
- color codes
- CR/LF/TAB
- bidi controls
- long Unicode string truncation
- malicious HTML-looking string

Display resolver:

- no meta values -> playerName
- one meta value -> playerName/value
- two meta values -> playerName/value1/value2
- three meta values -> playerName/value1/value2/value3
- missing middle value does not create double separators
- sanitized-empty meta value is omitted

Config parser:

- old `meta-key` only
- explicit empty `player-roster.luckperms-fields.fields` -> zero fields
- arbitrary roster keys such as `guild_name` and `event_rank`
- `display.fields` wins over `meta-key`
- blank keys skipped
- duplicate keys deduped
- more than three enabled fields truncated
- no valid configured fields -> Minecraft-ID-only roster with warning
- details default state `expanded` / `collapsed`

JSON serialization:

- schema v2 fields present
- display fields included
- roster fields included
- roster details settings included
- displayName, subName, and chips included
- metaValues include only present values
- UUID omitted
- players sorted
- bedrock true/false/null

File publisher:

- temp file write
- atomic move success path
- fallback path design if feasible

## Manual Integration Checklist

- PaperMC 26.1.2+ server starts with BlueMap, LuckPerms, and this plugin.
- No files are created under `plugins/BlueMap/`.
- No files are created under `plugins/LuckPerms/`.
- `plugins/BlueMapCommunityNames/web/overlay.js` exists.
- `plugins/BlueMapCommunityNames/web/overlay.css` exists.
- `plugins/BlueMapCommunityNames/web/players.json` exists.
- nginx `/bcn/overlay.js` returns 200.
- nginx `/bcn/players.json` returns 200 and `Cache-Control: no-store`.
- BlueMap native marker still shows `<JAVA_PLAYER_NAME>`.
- Overlay roster shows configured alias/chips when LuckPerms meta exists.
- Overlay roster shows `<JAVA_PLAYER_NAME>` when no configured meta values are present.
- Details toggle collapses and expands player chips without hiding player names.
- Browser-local opacity, density, and UI language settings persist across reload.
- LuckPerms meta change is reflected by event or periodic refresh.
- Player join/quit updates overlay.
- `/bluemap reload` causes script/style to be re-registered.
- Floodgate absent produces `bedrockDetection: "unavailable"` and does not break plugin.
- Malicious meta values do not execute or break UI.
- Plugin disable cancels tasks and stops async writer.

## Implementation Style

Keep code boring, explicit, and maintainable. Prefer small classes:

- `BlueMapCommunityNamesPlugin`
- `BlueMapRegistrar`
- `LuckPermsService`
- `FloodgateBridge`
- `PlayerSnapshotService`
- `JsonPublisher`
- `Sanitizer`
- `OverlayAssetManager`
- `BcnCommand`

No clever framework abstractions. Avoid noisy logs. Warnings should be clear when deployment
misconfiguration is likely.
