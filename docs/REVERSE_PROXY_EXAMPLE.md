# Optional Reverse Proxy Example

This page is an example only. BlueMapCommunityNames does not require nginx or any
specific reverse proxy. Adapt routing, paths, cache headers, TLS, firewall rules, and
service management to your own BlueMap/server environment.

The goal is to make the plugin-owned web files reachable from the same browser origin as
BlueMap under `/bcn/`.

Example nginx location:

```nginx
location ^~ /bcn/ {
    alias <SERVER_ROOT>/plugins/BlueMapCommunityNames/web/;
    add_header Cache-Control "no-store, no-cache, must-revalidate, max-age=0" always;
    expires off;
    try_files $uri =404;
}
```

The alias should point to:

```text
<SERVER_ROOT>/plugins/BlueMapCommunityNames/web/
```

Do not point it at BlueMap's webRoot. Do not copy BlueMapCommunityNames files into
`plugins/BlueMap/`.

## Existing BlueMap Routes

This route is additive. Do not break existing BlueMap routes, map assets, live data
routes, or upstream settings. BlueMap webserver and reverse-proxy behavior remain the
server administrator's responsibility.

## Checks

After applying your own webserver configuration:

```sh
curl -I <BLUEMAP_ORIGIN>/bcn/overlay.js
curl -I <BLUEMAP_ORIGIN>/bcn/overlay.css
curl -I <BLUEMAP_ORIGIN>/bcn/players.json
```

Expected:

- HTTP 200 for all three files.
- `players.json` has `Cache-Control: no-store` or equivalent.

If `/bcn/` is missing or wrong, BlueMap and the server-side plugin may still work, but
the browser overlay will not appear.

Java and Bedrock/Floodgate identities may be separate LuckPerms users and may need
separate meta values. BlueMap native markers are not modified by this plugin.
