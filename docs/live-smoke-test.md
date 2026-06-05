# BlueMapCommunityNames Live Smoke Test

This checklist is for a disposable PaperMC 26.1.2+ test server before production use.
Do not modify BlueMap, LuckPerms, Floodgate, or Geyser jars or data folders manually.

## Preconditions

- Java 25 is installed.
- PaperMC 26.1.2+ test server is available.
- BlueMap and LuckPerms are installed and working.
- Floodgate is optional.
- nginx can be configured for the test map domain.
- Build artifact exists:

```text
build/libs/BlueMapCommunityNames-0.2.0.jar
```

## Install On Test Server

1. Stop the test server.
2. Copy `BlueMapCommunityNames-0.2.0.jar` to the test server `plugins/` directory.
3. Start the server.
4. First-failure checks:
   - Confirm the server log accepts the `plugin.yml` descriptor. If Paper rejects
     `api-version: "26.1.2"`, stop here and fix the descriptor before further testing.
   - Confirm BlueMap and LuckPerms load before BlueMapCommunityNames.
   - Confirm BlueMapCommunityNames enables without errors.
   - Confirm `/plugins` shows BlueMapCommunityNames enabled.
   - From console, run `bcn status` and confirm the command works.
   - If Floodgate is absent, confirm status reports bedrock detection mode
     `unavailable` without errors.
   - If Floodgate is present, confirm the plugin name shown by `/plugins` or the server
     log matches this plugin's `softdepend: [floodgate]`. If it differs, stop and adjust
     the descriptor before relying on load ordering.
5. Confirm these generated files exist:

```text
plugins/BlueMapCommunityNames/web/overlay.js
plugins/BlueMapCommunityNames/web/overlay.css
plugins/BlueMapCommunityNames/web/players.json
```

6. Confirm no new plugin-owned files were created under:

```text
plugins/BlueMap/
plugins/LuckPerms/
plugins/floodgate/
plugins/Floodgate/
plugins/Geyser-Spigot/
```

## nginx Alias

Add the strict-clean alias for the test map domain:

```nginx
location ^~ /bcn/ {
    alias /path/to/server/plugins/BlueMapCommunityNames/web/;
    add_header Cache-Control "no-store, no-cache, must-revalidate, max-age=0" always;
    expires off;
    try_files $uri =404;
}
```

Reload nginx, then check:

```sh
curl -I https://your-test-map-domain.example/bcn/overlay.js
curl -I https://your-test-map-domain.example/bcn/players.json
```

Expected:

- `/bcn/overlay.js` returns `200`.
- `/bcn/players.json` returns `200`.
- `/bcn/players.json` includes `Cache-Control: no-store, no-cache, must-revalidate, max-age=0`.

If `/bcn/overlay.js` returns `404`, the nginx alias is missing or points to the wrong
directory.

## Command Status

Run:

```text
/bcn status
```

Expected output includes:

- BlueMap detected: yes
- LuckPerms detected: yes
- Floodgate detected: yes or no
- Bedrock detection mode: `floodgate` or `unavailable`
- Active roster fields: zero to three owner-configured LuckPerms meta keys
- Config fallback used: `yes` or `no`
- Plugin data folder
- Web assets folder
- Public base path `/bcn/`
- Last successful JSON write
- Last error
- Strict-clean mode enabled
- nginx alias warning and curl suggestions

If roster fields are explicitly empty or invalid-only, expected behavior is a warning
when appropriate and a Minecraft-ID-only roster. `community_name`, `title`, and `role`
are sample/default keys only.

## Roster Display Behavior

Before browser overlay verification, Codex should seed LuckPerms meta explicitly where
an online test identity exists. Without meta, the overlay can only prove the player-name
fallback path.

The sample smoke uses `community_name`, `title`, and `role`, but production owners may
choose any zero to three LuckPerms meta keys.

With Java player `<JAVA_PLAYER_NAME>` online, set:

```text
/lp user <JAVA_PLAYER_NAME> meta set community_name <ALIAS_SAMPLE>
/lp user <JAVA_PLAYER_NAME> meta set title <TITLE_SAMPLE>
/lp user <JAVA_PLAYER_NAME> meta set role <ROLE_SAMPLE>
/bcn rebuild
```

With Bedrock/Floodgate player `<BEDROCK_PLAYER_NAME>` online, first try:

```text
/lp user <BEDROCK_PLAYER_NAME> meta set community_name <BEDROCK_ALIAS_SAMPLE>
/lp user <BEDROCK_PLAYER_NAME> meta set title <BEDROCK_TITLE_SAMPLE>
/lp user <BEDROCK_PLAYER_NAME> meta set role <BEDROCK_ROLE_SAMPLE>
/bcn rebuild
```

If `<BEDROCK_PLAYER_NAME>` is rejected because of username syntax, use the
Floodgate/LuckPerms UUID observed during the smoke test:

```text
/lp user <BEDROCK_FLOODGATE_UUID> meta set community_name <BEDROCK_ALIAS_SAMPLE>
/lp user <BEDROCK_FLOODGATE_UUID> meta set title <BEDROCK_TITLE_SAMPLE>
/lp user <BEDROCK_FLOODGATE_UUID> meta set role <BEDROCK_ROLE_SAMPLE>
/bcn rebuild
```

Wait up to 10 seconds, or run:

```text
/bcn rebuild
```

Check:

- BlueMap native map markers still display `<JAVA_PLAYER_NAME>` and `<BEDROCK_PLAYER_NAME>`.
- The independent roster display matches `player-roster.name-display.mode`: alias primary,
  Minecraft ID primary with alias subtext/chip, or Minecraft-ID-only. Title/role chips
  appear when those values are present and configured.
- `players.json` has `schemaVersion: 2` and contains `playerName`, `minecraftId`,
  `displayName`, `subName`, `display`, `bedrock`, `chips`, `metaValues`, and `filterText`.
- `players.json` does not contain UUIDs.

Unset Java meta:

```text
/lp user <JAVA_PLAYER_NAME> meta unset community_name
```

Wait up to 10 seconds, or run `/bcn rebuild`.

Expected:

- BlueMap native map marker still displays `<JAVA_PLAYER_NAME>`.
- The roster displays remaining present chips, or `<JAVA_PLAYER_NAME>` if no configured
  values remain.
- Missing values are omitted from `metaValues`.

## Configurable Roster Fields

Change `plugins/BlueMapCommunityNames/config.yml`:

```yaml
player-roster:
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

Set all configured keys:

```text
/lp user <JAVA_PLAYER_NAME> meta set community_name <ALIAS_SAMPLE>
/lp user <JAVA_PLAYER_NAME> meta set title <TITLE_SAMPLE>
/lp user <JAVA_PLAYER_NAME> meta set role <ROLE_SAMPLE>
/bcn reload
/bcn rebuild
```

Expected `players.json` entry while `<JAVA_PLAYER_NAME>` is online:

```json
{
  "playerName": "<JAVA_PLAYER_NAME>",
  "minecraftId": "<JAVA_PLAYER_NAME>",
  "displayName": "<ALIAS_SAMPLE>",
  "subName": "@<JAVA_PLAYER_NAME>",
  "display": "<ALIAS_SAMPLE>",
  "bedrock": false,
  "chips": [
    {"id": "title", "key": "title", "label": "称号", "display": "chip", "value": "<TITLE_SAMPLE>"},
    {"id": "role", "key": "role", "label": "ロール", "display": "chip", "value": "<ROLE_SAMPLE>"}
  ],
  "metaValues": [
    {"id": "community_name", "key": "community_name", "label": "よび名", "display": "alias", "value": "<ALIAS_SAMPLE>"},
    {"id": "title", "key": "title", "label": "称号", "display": "chip", "value": "<TITLE_SAMPLE>"},
    {"id": "role", "key": "role", "label": "ロール", "display": "chip", "value": "<ROLE_SAMPLE>"}
  ]
}
```

Restore sample roster config:

```yaml
player-roster:
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
      label: "コミュニティ名"
      enabled: true
      filterable: true
    - key: "title"
      label: "肩書"
      enabled: true
      filterable: true
    - key: "role"
      label: "役割"
      enabled: true
      filterable: true
```

Then run:

```text
/bcn reload
/bcn rebuild
```

## BlueMap Reload

Run:

```text
/bluemap reload
```

Expected:

- BlueMap continues to load normally.
- The overlay script and style are re-registered.
- The overlay appears again after the BlueMap WebApp reloads.

## Owner/Player Stop Instructions

Codex must complete build, server startup, `/bcn status`, generated-file checks,
`players.json` schema checks, `/bcn/` curl checks, config reload checks, and static
clean-ownership checks before asking the owner to perform these manual phases.

### A. Java Connection And Overlay Display

- Java endpoint: `<TEST_SERVER_HOST>:<JAVA_PORT>`
- BlueMap proxy URL: `http://<TEST_SERVER_HOST>:<TEST_PROXY_PORT>/`
- Action: connect as `<JAVA_PLAYER_NAME>` from a Java client, then open the BlueMap URL.
- Expected native marker text: `<JAVA_PLAYER_NAME>`
- Expected roster: alias `<ALIAS_SAMPLE>`, subtext `@<JAVA_PLAYER_NAME>`, and
  chips for `<TITLE_SAMPLE>` and `<ROLE_SAMPLE>` when those values are present.
- Report back: whether the native marker stayed unchanged, whether the roster matched,
  and whether the overlay panel appeared without breaking BlueMap controls.

### B. Bedrock/Floodgate Connection And Overlay Display

- Bedrock endpoint: `<TEST_SERVER_HOST>:<BEDROCK_PORT>`
- BlueMap proxy URL: `http://<TEST_SERVER_HOST>:<TEST_PROXY_PORT>/`
- Action: connect as `<BEDROCK_PLAYER_NAME>` from a Bedrock client, then open the BlueMap URL.
- Expected native marker text: `<BEDROCK_PLAYER_NAME>`
- Expected roster: alias `<BEDROCK_ALIAS_SAMPLE>`, subtext `@<BEDROCK_PLAYER_NAME>`,
  and chips for `<BEDROCK_TITLE_SAMPLE>` and `<BEDROCK_ROLE_SAMPLE>` when those values are present.
- Report back: whether the native marker stayed unchanged, whether the roster matched,
  and whether Bedrock appears in the independent overlay.

### C. Browser UI Interaction

- BlueMap proxy URL: `http://<TEST_SERVER_HOST>:<TEST_PROXY_PORT>/`
- Actions: expand and collapse the roster, drag it by the header/collapsed bar, reset
  position, collapse and expand player details/chips, open browser-local settings,
  adjust opacity, density, and UI language, reload the browser, search by player name,
  search by meta value, filter by configured fields, remove one active filter chip, and
  clear filters.
- Expected result: roster and browser-local appearance state persist after reload,
  details collapse does not hide player names, filters combine with AND, visible count
  and total count are correct, and BlueMap native controls remain usable.
- Non-goal: the browser UI does not edit LuckPerms meta keys or server `config.yml`.
- Report back: which UI actions passed or failed, including the visible/total count.

### D. Malicious Value Browser Check

- BlueMap proxy URL: `http://<TEST_SERVER_HOST>:<TEST_PROXY_PORT>/`
- Action: after Codex seeds `<MALICIOUS_HTML_LIKE_META>`, reload the BlueMap page.
- Expected native marker text: unchanged player name.
- Expected overlay text: malicious-looking value appears as text only.
- Report back: whether any alert/script execution occurred and whether the UI stayed usable.

### E. Disconnect/Reconnect

- Java endpoint: `<TEST_SERVER_HOST>:<JAVA_PORT>`
- Bedrock endpoint: `<TEST_SERVER_HOST>:<BEDROCK_PORT>`
- BlueMap proxy URL: `http://<TEST_SERVER_HOST>:<TEST_PROXY_PORT>/`
- Action: disconnect and reconnect the Java and Bedrock clients.
- Expected result: the independent overlay and visible/total count update after join and
  quit; BlueMap native markers remain controlled by BlueMap.
- Report back: observed overlay text and count after each disconnect/reconnect.

## Join And Quit Updates

1. Record the timestamp of `plugins/BlueMapCommunityNames/web/players.json`.
2. Have a player join.
3. Confirm `players.json` updates after the debounce.
4. Have the player quit.
5. Confirm `players.json` updates again.

Expected:

- Join and quit update the JSON.
- No async Bukkit access warnings or plugin errors appear in the server log.

## Floodgate Absent

On a test server without Floodgate:

- Plugin enables normally.
- Console `bcn status` and player `/bcn status` show Floodgate detected: no.
- Console `bcn status` and player `/bcn status` show Bedrock detection mode: `unavailable`.
- `players.json` has `bedrockDetection: "unavailable"`.
- Per-player `bedrock` values are `null`.

## Floodgate Present

On a test server with Floodgate:

- Confirm `/plugins` or the server log reports the runtime plugin name.
- Confirm that name matches `softdepend: [floodgate]`.
- Confirm `bcn status` shows Floodgate detected: yes.
- Confirm `players.json` has `bedrockDetection: "floodgate"`.

## Malicious Meta Safety

Set a malicious-looking community name:

```text
/lp user <JAVA_PLAYER_NAME> meta set community_name <MALICIOUS_HTML_LIKE_META>
```

Expected:

- The overlay remains usable.
- No script executes.
- The value is rendered as text.
- Long values may be truncated according to config.

Also test control and formatting characters if practical:

```text
/lp user <JAVA_PLAYER_NAME> meta set community_name "<LEGACY_COLOR_CODED_MULTILINE_META>"
```

Expected:

- Legacy section-sign formatting is removed.
- CR/LF/TAB/control characters are removed.
- Empty-after-sanitize values display only the player name.

## Uninstall

1. Stop the test server.
2. Delete `plugins/BlueMapCommunityNames-0.2.0.jar` or `plugins/BlueMapCommunityNames.jar`.
3. Delete `plugins/BlueMapCommunityNames/`.
4. Remove the nginx `/bcn/` alias.
5. Reload nginx.
6. Start the test server.

Expected:

- BlueMap and LuckPerms still work normally.
- `/bcn/overlay.js` no longer resolves.
- No BlueMapCommunityNames-owned files remain outside `plugins/BlueMapCommunityNames/`.
