# BlueMapCommunityNames

BlueMapCommunityNames adds a small independent BlueMap WebApp player roster showing
currently online players with zero to three configurable LuckPerms display attributes.

It does not change BlueMap native marker names, hide markers, replace markers, rewrite
BlueMap's player-list DOM, or write into BlueMap or LuckPerms folders.

## Requirements

- PaperMC 26.1.2+
- Java 25
- Gradle wrapper is included and pins Gradle 9.5.0
- BlueMap
- LuckPerms
- nginx for the supported strict-clean web deployment
- Optional: Floodgate

## Build

Use Java 25:

```sh
./gradlew clean test build --no-daemon
```

The plugin jar is generated at:

```text
build/libs/BlueMapCommunityNames-0.2.0.jar
```

## Setup

1. Build the plugin jar.
2. Place `BlueMapCommunityNames-0.2.0.jar` in `plugins/`.
3. Start the server once to generate `plugins/BlueMapCommunityNames/`.
4. Add an nginx alias for `/bcn/`.
5. Reload nginx.
6. Run `/bluemap reload` or restart the server so BlueMap registers the overlay script and style.

```nginx
location ^~ /bcn/ {
    alias /path/to/server/plugins/BlueMapCommunityNames/web/;
    add_header Cache-Control "no-store, no-cache, must-revalidate, max-age=0" always;
    expires off;
    try_files $uri =404;
}
```

Existing BlueMap `/maps/*/live/*` proxy settings should remain unchanged.

LuckPerms example using the default sample fields:

```text
/lp user <JAVA_PLAYER_NAME> meta set community_name <JAVA_COMMUNITY_NAME>
/lp user <JAVA_PLAYER_NAME> meta set title <JAVA_TITLE>
/lp user <JAVA_PLAYER_NAME> meta set role <JAVA_ROLE>
/lp user <BEDROCK_FLOODGATE_UUID> meta set community_name <BEDROCK_COMMUNITY_NAME>
/lp user <BEDROCK_FLOODGATE_UUID> meta set title <BEDROCK_TITLE>
/lp user <BEDROCK_FLOODGATE_UUID> meta set role <BEDROCK_ROLE>
```

`community_name`, `title`, and `role` are examples only. Server owners can configure
zero to three arbitrary LuckPerms meta keys. With zero fields, the roster shows Minecraft
IDs only and has no LuckPerms filters. Alias and chip fields are optional, and missing,
blank, or sanitized-empty values are omitted safely.

```yaml
meta-key: "community_name"

player-roster:
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
```

Existing v0.1 configs with only `meta-key` still work as a one-field alias roster.
Existing v0.2 `display.fields` configs are adapted when `player-roster.luckperms-fields`
is missing. If `player-roster.luckperms-fields.fields` is explicitly empty, the roster
uses Minecraft IDs only.

Java and Bedrock/Floodgate identities may be separate LuckPerms users. If a Floodgate
prefixed name such as `<BEDROCK_PLAYER_NAME>` is rejected by LuckPerms command parsing,
use the Floodgate UUID shown in the server log or LuckPerms data.

## Troubleshooting

Check nginx:

```sh
curl -I https://your-map-domain.example/bcn/overlay.js
curl -I https://your-map-domain.example/bcn/players.json
```

A 404 on `/bcn/overlay.js` means the nginx alias is missing or points at the wrong
directory. If names are stale, run `/bcn rebuild` and check `/bcn status`. If the overlay
is missing after changing paths, run `/bluemap reload` because BlueMap script/style
registration happens when BlueMap enables.

Floodgate unavailable is non-fatal. JSON will contain `bedrockDetection: "unavailable"`
and per-player `bedrock: null`.

If the active meta key is missing, blank, or blank after sanitization, the overlay falls
back to the plain player name. If some configured fields are missing, the overlay shows
only the present values. If JSON writing fails, `/bcn status` reports the last error and
the previous `players.json` remains in place when possible.

The browser overlay is a lightweight roster: collapsed by default, expandable, draggable,
searchable, and filterable with chip controls when configured filterable fields currently
have online values. Filtering is client-side over the current online player JSON and
never changes BlueMap native markers.

The Details control collapses or expands the displayed LuckPerms chip area for all
player rows. Player names and Minecraft ID subtext remain visible, and rows with hidden
chips show a small details count.

The browser Settings control is local to the current browser. It can adjust panel
opacity, compact/comfortable density, and UI language through CSS variables and
localStorage. It does not edit `config.yml`, does not change LuckPerms keys, and does
not send admin changes to the server.

To change which LuckPerms meta keys are shown, edit
`plugins/BlueMapCommunityNames/config.yml` on the server and run `/bcn reload`.
Browser-side LuckPerms key editing is intentionally not implemented.

Filter values are sorted by current online count descending, then by value. Each field
section can collapse independently. Only the first configured number of values is shown
initially, and high-cardinality fields provide an in-section value search instead of
dumping every value into the panel.

If `/bcn/` is missing from nginx, the server-side plugin continues to work and BlueMap
continues to load, but the browser overlay will not appear. Run `/bluemap reload` after
changing the script/style public path because BlueMap registers those paths during its
enable/reload cycle.

During smoke testing, confirm the installed Floodgate runtime plugin name from `/plugins`
or the server log. This plugin declares `softdepend: [floodgate]`, which matches the
commonly observed Spigot/Paper Floodgate plugin name, but the installed jar should be
checked before relying on load ordering.

## Commands

- `/bcn status`
- `/bcn rebuild`
- `/bcn reload`

Permission: `bluemapcommunitynames.admin`

## Uninstall

1. Stop server.
2. Delete `plugins/BlueMapCommunityNames.jar`.
3. Delete `plugins/BlueMapCommunityNames/`.
4. Remove nginx `/bcn/` alias.
5. Reload nginx.
6. Start server.

Removing only the jar disables the plugin, but deleting both the jar and
`plugins/BlueMapCommunityNames/` removes all plugin-owned files.

## BlueMapAPI Dependency Note

The official BlueMapAPI GitHub latest release checked on 2026-06-01 is `v2.7.8`.
The official wiki dependency snippet still showed `2.7.7`, so this project uses
`de.bluecolored:bluemap-api:2.7.8` from `https://repo.bluecolored.de/releases` and also
supports a local `libs/BlueMapAPI-*.jar` compileOnly fallback.
