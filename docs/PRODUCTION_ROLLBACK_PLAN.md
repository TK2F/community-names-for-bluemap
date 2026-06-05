# Rollback Plan

This rollback plan covers only BlueMapCommunityNames. It does not roll back BlueMap,
LuckPerms, Geyser/Floodgate, webserver, firewall, TLS, DNS, or operating-system changes.

## When To Roll Back

Roll back BlueMapCommunityNames if:

- the plugin fails to load
- the roster overlay breaks the BlueMap browser experience
- `/bcn status` reports persistent JSON write errors
- the optional `/bcn/` route was added incorrectly and the overlay cannot load
- unexpected server errors are clearly related to BlueMapCommunityNames

## Rollback Steps

1. Stop the server if required by your operation policy.
2. Remove the BlueMapCommunityNames jar from `plugins/`.
3. Optionally remove `plugins/BlueMapCommunityNames/` after backing up any local config
   you want to keep.
4. Remove any optional `/bcn/` webserver/reverse-proxy route added for this plugin.
5. Start the server if it was stopped.
6. Verify BlueMap and LuckPerms still work normally.

## Rollback Verification

Verify:

- BlueMap still loads.
- LuckPerms commands and data remain unaffected.
- BlueMap native live markers still work.
- no BlueMapCommunityNames-owned files remain under BlueMap, LuckPerms, Geyser, or
  Floodgate folders.

Rollback should remove only BlueMapCommunityNames-owned files and any optional public
route added specifically for `/bcn/`.
