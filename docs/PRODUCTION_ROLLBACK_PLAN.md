# Production Rollback Plan

## When To Roll Back

Roll back if any of these occur:

- BlueMapCommunityNames fails to load.
- The overlay breaks the BlueMap browser UI.
- The nginx `/bcn/` route or related config breaks existing BlueMap access.
- Unexpected server errors appear after deploy.
- `/bcn status` reports persistent JSON write errors.

## Rollback Steps

1. If the server is unstable or a restart is already planned, stop the server.
2. Remove the plugin jar:

```sh
rm <PRODUCTION_SERVER_PATH>/plugins/BlueMapCommunityNames-0.2.0.jar
```

3. Remove the plugin data folder:

```sh
rm -rf <PRODUCTION_SERVER_PATH>/plugins/BlueMapCommunityNames/
```

4. Remove the nginx `/bcn/` alias from:

```text
<PRODUCTION_NGINX_CONFIG_PATH>
```

5. Test nginx config:

```sh
nginx -t -c <PRODUCTION_NGINX_CONFIG_PATH>
```

6. Reload nginx:

```sh
nginx -s reload
```

7. Start or restart the server if it was stopped.

## Rollback Verification

Verify:

- BlueMap still loads at `<PRODUCTION_BLUEMAP_URL>`.
- LuckPerms commands and data remain unaffected.
- `/bcn/overlay.js` is no longer served.
- BlueMap native live markers still work.
- Server log has no new BlueMapCommunityNames errors.
- `plugins/BlueMapCommunityNames/` no longer exists.
- No BlueMapCommunityNames-owned files remain under BlueMap, LuckPerms, Geyser, or
  Floodgate folders.

Rollback removes only BlueMapCommunityNames-owned files and the `/bcn/` public route.
