# Example Operational Install Plan

This document is a public-safe example for administrators. It is not a universal
production deployment plan. BlueMapCommunityNames does not own your BlueMap webserver,
LuckPerms setup, Geyser/Floodgate identity model, nginx/reverse proxy, firewall, TLS, or
network exposure.

## Scope

BlueMapCommunityNames installs as a normal Paper plugin and writes only under:

```text
plugins/BlueMapCommunityNames/
```

It must not write under:

```text
plugins/BlueMap/
plugins/LuckPerms/
plugins/Geyser-Spigot/
plugins/floodgate/
```

BlueMap native live markers are not modified.

## Install Steps

1. Confirm BlueMap works before installing this plugin.
2. Confirm LuckPerms works before installing this plugin.
3. Stop the server if your operation policy requires offline plugin installation.
4. Copy `BlueMapCommunityNames-0.2.0.jar` into the server `plugins/` directory.
5. Start the server.
6. Confirm BlueMapCommunityNames enables after BlueMap and LuckPerms.
7. Run:

```text
bcn status
```

Expected:

- BlueMap detected.
- LuckPerms detected.
- Public base path is `/bcn/` by default.
- Last error is `none`.
- Active roster fields are zero to three configured LuckPerms meta keys.

Generated files:

```text
plugins/BlueMapCommunityNames/web/overlay.js
plugins/BlueMapCommunityNames/web/overlay.css
plugins/BlueMapCommunityNames/web/players.json
```

## Optional Web Route

Some BlueMap web setups need an explicit route for `/bcn/`. nginx is one possible
solution, but it is not required by this plugin. If you use a webserver or reverse proxy,
adapt the route to your own environment.

See [Optional Reverse Proxy Example](REVERSE_PROXY_EXAMPLE.md).

## LuckPerms Setup

Default sample fields are:

- `community_name`
- `title`
- `role`

These are examples only. Server owners may configure zero to three arbitrary LuckPerms
meta keys in `plugins/BlueMapCommunityNames/config.yml`.

Example placeholder commands:

```text
/lp user <JAVA_PLAYER_NAME> meta set nickname <ALIAS_SAMPLE>
/lp user <JAVA_PLAYER_NAME> meta set guild_name <GUILD_SAMPLE>
/lp user <JAVA_PLAYER_NAME> meta set event_rank <EVENT_RANK_SAMPLE>
```

Java and Bedrock/Floodgate identities may be separate LuckPerms users. Depending on your
setup, Bedrock players may need UUID-based targeting. Do not publish real UUIDs.

After config changes:

```text
bcn reload
bcn rebuild
```

## Browser Verification

- Open your BlueMap web URL.
- Confirm BlueMap loads normally.
- Confirm the BlueMapCommunityNames roster appears.
- Confirm search, filters, details collapse, settings, opacity, and language switch work.
- Confirm no empty chips or duplicate alias/Minecraft ID display appears.

## Ownership Check

After install, verify BlueMapCommunityNames-owned files are only under:

```text
plugins/BlueMapCommunityNames/
```

BlueMap, LuckPerms, Geyser, and Floodgate folders may contain their own normal runtime
files, but this plugin should not create files there.
