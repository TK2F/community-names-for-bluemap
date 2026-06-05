# CommunityNames for BlueMap Release Notes v0.2.0

## Highlights

- Adds a BlueMap player roster overlay backed by LuckPerms meta.
- Supports zero to three arbitrary LuckPerms fields.
- Adds display modes:
  - `alias_as_primary`
  - `minecraft_id_as_primary`
  - `minecraft_id_only`
- Keeps BlueMap native markers unchanged.
- Generates plugin-owned web assets under `plugins/BlueMapCommunityNames/web/`.
- Supports optional Floodgate detection when available.

## Compatibility

Validated with Paper 26.1.2, Java 25, BlueMap, LuckPerms, and optional Geyser/Floodgate.

## Notes

- BlueMap and LuckPerms must be installed separately.
- Geyser/Floodgate are optional for Bedrock support.
- nginx or reverse proxy setup is optional and administrator-managed.
- BlueMapCommunityNames is independent and is not affiliated with, endorsed by, or
  supported by Mojang, Microsoft, Minecraft, BlueMap, LuckPerms, Geyser, or Floodgate.

Before publishing a release, re-verify upstream licenses and finalize artifact/tag
strategy.
