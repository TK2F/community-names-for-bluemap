# Operational Checklist

This is a public-safe checklist for administrators installing BlueMapCommunityNames in
their own environment. It is not a deployment guarantee and does not replace BlueMap,
LuckPerms, Geyser/Floodgate, nginx, firewall, TLS, or server operations documentation.

## Before Installing

- [ ] BlueMap is installed and working independently.
- [ ] LuckPerms is installed and working independently.
- [ ] Geyser/Floodgate are installed only if Bedrock support is desired.
- [ ] Server restart or reload expectations are understood.
- [ ] Rollback plan is ready.
- [ ] Any webserver or reverse-proxy changes are reviewed by the server administrator.
- [ ] No private values will be committed to Git or shared publicly.

## Install

- [ ] Build or obtain the BlueMapCommunityNames jar.
- [ ] Place the jar in the server `plugins/` directory.
- [ ] Start or restart the server.
- [ ] Confirm BlueMapCommunityNames is enabled.
- [ ] Run `bcn status`.
- [ ] Confirm generated files exist under `plugins/BlueMapCommunityNames/web/`.
- [ ] Confirm no BlueMapCommunityNames-owned files were created under BlueMap, LuckPerms,
      Geyser, or Floodgate folders.

## Configure

- [ ] Choose zero to three LuckPerms meta keys.
- [ ] Confirm `community_name`, `title`, and `role` are examples only.
- [ ] Choose a display mode: `alias_as_primary`, `minecraft_id_as_primary`, or
      `minecraft_id_only`.
- [ ] Run `bcn reload` after config changes.
- [ ] Run `bcn rebuild` if you need immediate JSON refresh.

## Optional Web Route

If your BlueMap web environment requires an explicit `/bcn/` route:

- [ ] Add the route according to your own webserver/reverse-proxy environment.
- [ ] Confirm the route points to `plugins/BlueMapCommunityNames/web/`.
- [ ] Do not copy files into BlueMap's webRoot.
- [ ] Confirm `/bcn/overlay.js`, `/bcn/overlay.css`, and `/bcn/players.json` return 200.
- [ ] Confirm `players.json` uses `no-store` or equivalent short/no caching.

nginx is not required by BlueMapCommunityNames; it is only one possible webserver choice.

## Java Verification

- [ ] Java player joins.
- [ ] Roster shows the Java player.
- [ ] Native BlueMap marker remains the original player identity if visible.
- [ ] Configured alias/chips appear when LuckPerms meta exists.
- [ ] Missing meta values are omitted safely.

## Bedrock / Floodgate Verification

- [ ] Bedrock player joins if Bedrock support is in scope.
- [ ] Roster shows the Bedrock player.
- [ ] Floodgate identity is understood for your server.
- [ ] If username targeting fails in LuckPerms, use UUID-based targeting locally.
- [ ] Do not publish real UUIDs.

## Browser Verification

- [ ] BlueMap loads normally.
- [ ] BlueMapCommunityNames overlay appears.
- [ ] Search works.
- [ ] Filters work when filterable values exist.
- [ ] Details/chips collapse works.
- [ ] Settings and opacity work.
- [ ] Japanese/English switch works.
- [ ] No empty chips appear.
- [ ] Alias/Minecraft ID is not duplicated.

## Rollback Ready

- [ ] Plugin jar removal path is known.
- [ ] Plugin data folder removal path is known.
- [ ] Optional `/bcn/` route removal path is known.
- [ ] LuckPerms test meta unset commands are known.
- [ ] BlueMap and LuckPerms can be verified after rollback.
