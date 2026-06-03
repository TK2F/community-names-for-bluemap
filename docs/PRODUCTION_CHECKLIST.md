# Production Checklist

## Pre-Deploy

- [ ] Confirm maintenance window if restart is needed.
- [ ] Record Paper version.
- [ ] Record BlueMap version.
- [ ] Record LuckPerms version.
- [ ] Record Geyser/Floodgate presence.
- [ ] Record `<PRODUCTION_BLUEMAP_URL>`.
- [ ] Record `<PRODUCTION_NGINX_CONFIG_PATH>`.
- [ ] Back up nginx config.
- [ ] Save current plugin list.
- [ ] Confirm rollback plan is ready.

## Deploy

- [ ] Copy `<PLUGIN_JAR_PATH>` to `<PRODUCTION_SERVER_PATH>/plugins/BlueMapCommunityNames-0.2.0.jar`.
- [ ] Start or restart server.
- [ ] Confirm BlueMapCommunityNames is enabled.
- [ ] Run `bcn status`.
- [ ] Confirm active roster fields are the expected zero to three LuckPerms meta keys.
- [ ] Confirm generated files exist under `plugins/BlueMapCommunityNames/web/`.

## nginx

- [ ] Add `/bcn/` alias to `<PRODUCTION_NGINX_CONFIG_PATH>`.
- [ ] Confirm alias points to `<PRODUCTION_SERVER_PATH>/plugins/BlueMapCommunityNames/web/`.
- [ ] Do not change existing BlueMap `/maps/*` or live proxy behavior.
- [ ] Run nginx config test.
- [ ] Reload nginx.
- [ ] `curl -I <PRODUCTION_BLUEMAP_URL>/bcn/overlay.js` returns 200.
- [ ] `curl -I <PRODUCTION_BLUEMAP_URL>/bcn/overlay.css` returns 200.
- [ ] `curl -I <PRODUCTION_BLUEMAP_URL>/bcn/players.json` returns 200.
- [ ] `/bcn/players.json` has `Cache-Control: no-store` or equivalent.

## LuckPerms Meta

- [ ] Set Java player meta if needed:
  `/lp user <JAVA_PLAYER_NAME> meta set community_name <JAVA_COMMUNITY_NAME>`
- [ ] Set Bedrock/Floodgate player meta if needed:
  `/lp user <BEDROCK_PLAYER_NAME> meta set community_name <BEDROCK_COMMUNITY_NAME>`
- [ ] If Bedrock/Floodgate prefixed username is rejected, use UUID targeting.
- [ ] Remember Java and Bedrock/Floodgate identities may be separate LuckPerms users.
- [ ] If using custom fields, update `player-roster.luckperms-fields.fields`, run
  `bcn reload`, then run `bcn rebuild`.

## Browser Verification

- [ ] Open `<PRODUCTION_BLUEMAP_URL>`.
- [ ] Confirm BlueMap loads normally.
- [ ] Confirm BlueMapCommunityNames overlay appears.
- [ ] Confirm no UI breakage.
- [ ] Confirm `/bcn/players.json` loads in browser.

## Java Player Verification

- [ ] Java player joins.
- [ ] Native BlueMap marker remains `<JAVA_PLAYER_NAME>`.
- [ ] Roster shows configured alias/chips when meta exists.
- [ ] Roster falls back to `<JAVA_PLAYER_NAME>` when no configured values are present.
- [ ] `players.json` has `bedrock:false`.

## Bedrock Player Verification

- [ ] Bedrock/Floodgate player joins if available.
- [ ] Native BlueMap marker remains `<BEDROCK_PLAYER_NAME>`.
- [ ] Roster shows configured alias/chips when meta exists.
- [ ] Roster falls back to `<BEDROCK_PLAYER_NAME>` when no configured values are present.
- [ ] `players.json` has `bedrock:true` when Floodgate detects Bedrock.

## Post-Deploy

- [ ] Run `bcn rebuild`.
- [ ] Run `bcn status`.
- [ ] Last error is `none`.
- [ ] `players.json` has `schemaVersion: 2`.
- [ ] `players.json` does not expose UUIDs by default.
- [ ] Server log has no BlueMapCommunityNames errors.
- [ ] No BlueMapCommunityNames-owned files under BlueMap/LuckPerms/Geyser/Floodgate folders.
- [ ] `bluemap reload` re-registers script/style.

## Rollback Ready

- [ ] Rollback plan reviewed.
- [ ] nginx backup available.
- [ ] Plugin jar removal path known.
- [ ] Plugin data folder removal path known.
- [ ] `/bcn/` alias removal path known.
