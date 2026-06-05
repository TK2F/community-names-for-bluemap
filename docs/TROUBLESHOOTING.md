# Troubleshooting

## Overlay Does Not Appear

Check whether the browser can fetch plugin-owned files:

```sh
curl -I <BLUEMAP_ORIGIN>/bcn/overlay.js
curl -I <BLUEMAP_ORIGIN>/bcn/overlay.css
curl -I <BLUEMAP_ORIGIN>/bcn/players.json
```

If these return 404, your BlueMap web environment may not expose the plugin-owned
`/bcn/` files. Configure a route appropriate for your environment.

## BlueMap Works But Roster Is Empty

- Confirm players are online.
- Run `bcn status`.
- Run `bcn rebuild`.
- Confirm LuckPerms is loaded.
- Confirm configured meta keys exist for the relevant players.

## Missing Chips

Missing, blank, sanitized-empty, or duplicate alias values are omitted intentionally.
Filters are created only from configured filterable fields that currently have online
values.

## Bedrock Meta Does Not Apply

Java and Bedrock/Floodgate identities may be separate LuckPerms users. If username
targeting fails for a Bedrock player, UUID-based targeting may be required in your
LuckPerms setup. Do not publish real UUIDs.

## Browser Looks Stale

Use a private/incognito window, hard refresh, or clear localStorage for the BlueMap
origin. Browser settings such as opacity and language are local to the browser.

## BlueMap Native Markers

BlueMapCommunityNames does not mutate BlueMap native markers. If native markers look
wrong, review BlueMap and server configuration separately.
