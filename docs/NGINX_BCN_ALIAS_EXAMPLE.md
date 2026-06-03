# nginx `/bcn/` Alias Example

This example is for the strict-clean deployment model. Adapt all paths and server blocks
to production before use.

```nginx
location ^~ /bcn/ {
    alias <PRODUCTION_SERVER_PATH>/plugins/BlueMapCommunityNames/web/;
    add_header Cache-Control "no-store, no-cache, must-revalidate, max-age=0" always;
    expires off;
    try_files $uri =404;
}
```

The alias must point to:

```text
<PRODUCTION_SERVER_PATH>/plugins/BlueMapCommunityNames/web/
```

Do not point it at BlueMap's webRoot. Do not copy BlueMapCommunityNames files into
`plugins/BlueMap/`.

## Existing BlueMap Routes

Do not break existing BlueMap routes such as:

- map assets under `/maps/`
- live data routes
- existing reverse proxy or upstream settings

The `/bcn/` alias is additive. Existing BlueMap `/maps/*` and live proxy behavior should
remain unchanged.

## Checks

After reloading nginx:

```sh
curl -I <PRODUCTION_BLUEMAP_URL>/bcn/overlay.js
curl -I <PRODUCTION_BLUEMAP_URL>/bcn/overlay.css
curl -I <PRODUCTION_BLUEMAP_URL>/bcn/players.json
```

Expected:

- HTTP 200 for all three files.
- `players.json` has `Cache-Control: no-store` or equivalent.

If `/bcn/` is missing or wrong, BlueMap and the server-side plugin should still work,
but the browser overlay will not appear.

`community_name`, `title`, and `role` are example LuckPerms meta keys only. If roster
fields are changed in config, run:

```text
bcn reload
bcn rebuild
```

Java and Bedrock/Floodgate identities may be separate LuckPerms users and may need
separate meta values. BlueMap native markers are not modified by this plugin.
