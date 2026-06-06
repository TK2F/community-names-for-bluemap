# Live Smoke Test Checklist

Use this checklist on a disposable or owner-approved server before relying on
BlueMapCommunityNames in routine operation.

Do not modify BlueMap, LuckPerms, Floodgate, or Geyser jars or data folders manually.
Do not publish real player names, UUIDs, local IPs, or private server details in smoke
test reports.

## Preconditions

- Paper-compatible server is available.
- Java 25 is available.
- BlueMap is installed and working independently.
- LuckPerms is installed and working independently.
- Geyser/Floodgate are installed only if Bedrock support is in scope.
- BlueMapCommunityNames jar exists.

## Install On Test Server

1. Stop the server if required by your operation policy.
2. Copy `BlueMapCommunityNames-0.2.1.jar` to the server `plugins/` directory.
3. Start the server.
4. Confirm BlueMap and LuckPerms load before BlueMapCommunityNames.
5. Confirm BlueMapCommunityNames enables without errors.
6. Run:

```text
bcn status
```

7. Confirm generated files exist:

```text
plugins/BlueMapCommunityNames/web/overlay.js
plugins/BlueMapCommunityNames/web/overlay.css
plugins/BlueMapCommunityNames/web/players.json
```

8. Confirm no BlueMapCommunityNames-owned files were created under:

```text
plugins/BlueMap/
plugins/LuckPerms/
plugins/floodgate/
plugins/Geyser-Spigot/
```

## Optional `/bcn/` Web Route

If your BlueMap web setup requires an explicit route, configure `/bcn/` according to your
own server environment. nginx is optional and is only one possible implementation.

Expected checks:

```sh
curl -I <BLUEMAP_ORIGIN>/bcn/overlay.js
curl -I <BLUEMAP_ORIGIN>/bcn/overlay.css
curl -I <BLUEMAP_ORIGIN>/bcn/players.json
```

Expected:

- all three files return HTTP 200
- `players.json` uses `no-store` or equivalent

## Java Smoke

With a Java player online:

- roster row appears
- search by Minecraft ID works
- configured alias/chips appear when LuckPerms meta exists
- missing values are omitted
- native BlueMap marker remains the original identity if visible

Use placeholders in reports:

```text
<JAVA_PLAYER_NAME>
<ALIAS_SAMPLE>
<GUILD_SAMPLE>
<EVENT_RANK_SAMPLE>
```

## Bedrock / Floodgate Smoke

With a Bedrock player online, if Bedrock support is in scope:

- Floodgate identity is detected when available
- roster row appears
- search works
- configured alias/chips appear when LuckPerms meta exists
- native BlueMap marker remains the original identity if visible

Depending on your Floodgate/LuckPerms setup, UUID-based targeting may be needed. Do not
publish real UUIDs.

## Display Mode Matrix

Test the configured server culture:

- `alias_as_primary`
- `minecraft_id_as_primary`
- `minecraft_id_only`
- zero fields
- one to three arbitrary LuckPerms fields

Expected:

- no empty chips
- no duplicate alias/Minecraft ID display
- filters appear only for configured filterable values currently online
- search includes configured searchable values

## Browser UI

Check:

- BlueMap map remains usable
- roster appears
- collapsed/expanded panel works
- details/chips collapse works
- settings popover works
- opacity works
- Japanese/English switch works
- no empty UI artifacts

## Cleanup

- unset any synthetic LuckPerms meta
- stop the test server if it is disposable
- remove test-only plugin files if desired
- keep private runtime logs out of Git and public reports
