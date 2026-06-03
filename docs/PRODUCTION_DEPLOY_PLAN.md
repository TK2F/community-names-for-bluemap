# Production Deploy Plan

## Scope

BlueMapCommunityNames is an internal/private Paper plugin. This plan assumes no public
release, no GitHub release, and no tag creation.

The deployment uses the strict-clean ownership model:

- The plugin may write only under `<PRODUCTION_SERVER_PATH>/plugins/BlueMapCommunityNames/`.
- The plugin must not write under `<PRODUCTION_SERVER_PATH>/plugins/BlueMap/`.
- The plugin must not write under `<PRODUCTION_SERVER_PATH>/plugins/LuckPerms/`.
- The plugin must not write under Geyser or Floodgate plugin folders.
- nginx or an equivalent same-origin reverse proxy exposes `/bcn/` to
  `<PRODUCTION_SERVER_PATH>/plugins/BlueMapCommunityNames/web/`.

BlueMap native live markers are not modified.

## Pre-Deploy Checks

Record the current production state before making changes:

```text
Production server path: <PRODUCTION_SERVER_PATH>
Production BlueMap URL: <PRODUCTION_BLUEMAP_URL>
Production nginx config path: <PRODUCTION_NGINX_CONFIG_PATH>
Paper version:
BlueMap version:
LuckPerms version:
Geyser present:
Floodgate present:
```

Prepare rollback inputs:

```sh
cp <PRODUCTION_NGINX_CONFIG_PATH> <PRODUCTION_NGINX_CONFIG_PATH>.pre-bcn-backup
ls -la <PRODUCTION_SERVER_PATH>/plugins > <PRODUCTION_SERVER_PATH>/plugins.pre-bcn-list.txt
```

Confirm:

- Maintenance window is approved if a server restart is needed.
- Current BlueMap loads at `<PRODUCTION_BLUEMAP_URL>`.
- LuckPerms is healthy.
- nginx config test command is known for the production host.
- Rollback plan is available before deploy.

## Artifact

Deploy artifact:

```text
build/libs/BlueMapCommunityNames-0.2.0.jar
```

Production destination:

```text
<PRODUCTION_SERVER_PATH>/plugins/BlueMapCommunityNames-0.2.0.jar
```

## Server Deploy Steps

Preferred low-risk flow:

1. Stop the server during the maintenance window.
2. Copy the jar:

```sh
cp build/libs/BlueMapCommunityNames-0.2.0.jar <PRODUCTION_SERVER_PATH>/plugins/BlueMapCommunityNames-0.2.0.jar
```

3. Start the server.
4. Confirm the plugin loads after BlueMap and LuckPerms.
5. Run from console:

```text
bcn status
```

Expected:

- BlueMap detected: yes
- LuckPerms detected: yes
- Active roster fields show zero to three configured LuckPerms meta keys
- Public base path: `/bcn/`
- Last error: `none`

6. Verify generated files:

```text
<PRODUCTION_SERVER_PATH>/plugins/BlueMapCommunityNames/web/overlay.js
<PRODUCTION_SERVER_PATH>/plugins/BlueMapCommunityNames/web/overlay.css
<PRODUCTION_SERVER_PATH>/plugins/BlueMapCommunityNames/web/players.json
```

If roster fields are explicitly empty or invalid-only, the plugin warns when appropriate
and uses a Minecraft-ID-only roster. `community_name`, `title`, and `role` are examples
only, not required fields.

## nginx Deploy Steps

Add a `/bcn/` alias to the production BlueMap server block. Adapt paths to production:

```nginx
location ^~ /bcn/ {
    alias <PRODUCTION_SERVER_PATH>/plugins/BlueMapCommunityNames/web/;
    add_header Cache-Control "no-store, no-cache, must-revalidate, max-age=0" always;
    expires off;
    try_files $uri =404;
}
```

Do not change existing BlueMap `/maps/*`, live-data, or upstream proxy behavior.

Test and reload nginx:

```sh
nginx -t -c <PRODUCTION_NGINX_CONFIG_PATH>
nginx -s reload
```

Run:

```sh
curl -I <PRODUCTION_BLUEMAP_URL>/bcn/overlay.js
curl -I <PRODUCTION_BLUEMAP_URL>/bcn/overlay.css
curl -I <PRODUCTION_BLUEMAP_URL>/bcn/players.json
```

Expected:

- `overlay.js`: HTTP 200
- `overlay.css`: HTTP 200
- `players.json`: HTTP 200
- `players.json`: `Cache-Control: no-store` or equivalent

If `/bcn/` is missing, the server-side plugin still works and BlueMap still loads, but
the browser overlay will not appear.

## LuckPerms Setup

Default example roster fields:

```yaml
meta-key: "community_name"

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

`community_name`, `title`, and `role` are examples only. Server owners may configure
zero to three arbitrary LuckPerms meta keys in:

```text
<PRODUCTION_SERVER_PATH>/plugins/BlueMapCommunityNames/config.yml
```

After changing fields:

```text
bcn reload
bcn rebuild
```

Example commands for the default sample keys:

```text
/lp user <JAVA_PLAYER_NAME> meta set community_name <JAVA_COMMUNITY_NAME>
/lp user <JAVA_PLAYER_NAME> meta set title <JAVA_TITLE>
/lp user <JAVA_PLAYER_NAME> meta set role <JAVA_ROLE>
/lp user <BEDROCK_PLAYER_NAME> meta set community_name <BEDROCK_COMMUNITY_NAME>
/lp user <BEDROCK_PLAYER_NAME> meta set title <BEDROCK_TITLE>
/lp user <BEDROCK_PLAYER_NAME> meta set role <BEDROCK_ROLE>
```

Java and Bedrock/Floodgate identities may be separate LuckPerms users. Both may need
separate meta values. Missing values are omitted safely.

If a Bedrock/Floodgate prefixed username is rejected by LuckPerms command parsing, use
UUID targeting:

```text
/lp user <BEDROCK_FLOODGATE_UUID> meta set community_name <BEDROCK_COMMUNITY_NAME>
```

## Browser Verification

Open:

```text
<PRODUCTION_BLUEMAP_URL>
```

Expected:

- BlueMap loads normally.
- BlueMapCommunityNames overlay appears.
- Native BlueMap marker remains `<JAVA_PLAYER_NAME>` or `<BEDROCK_PLAYER_NAME>`.
- Roster shows the configured alias as the main name and configured chip fields when
  those values are present.
- If no configured values are present for a player, the roster falls back to the
  Minecraft player name.

## Post-Deploy Checks

Run:

```text
bcn status
bcn rebuild
bcn status
```

Check:

- `players.json` is valid JSON.
- `schemaVersion` is `2`.
- No UUIDs are emitted by default.
- Last error is `none`.
- Server log has no BlueMapCommunityNames errors.
- No BlueMapCommunityNames-owned files were written under:
  - `<PRODUCTION_SERVER_PATH>/plugins/BlueMap/`
  - `<PRODUCTION_SERVER_PATH>/plugins/LuckPerms/`
  - Geyser/Floodgate plugin folders

If BlueMap is reloaded:

```text
bluemap reload
```

Expected:

- BlueMap reloads normally.
- BlueMapCommunityNames script/style registration runs again.
- `/bcn/` assets remain served by nginx.
