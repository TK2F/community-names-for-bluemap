# Compatibility

## Tested Scope

BlueMapCommunityNames has been validated with:

- Paper 26.1.2
- Java 25
- BlueMap 5.20
- LuckPerms 5.5.x
- Geyser/Floodgate for optional Bedrock smoke testing

One WSL2 validation used local nginx with same-origin `/bcn/` routing. This is not a
requirement and does not mean every nginx or reverse-proxy topology is supported by this
plugin.

## Required Dependencies

- BlueMap
- LuckPerms

Install both separately.

## Optional Dependencies

- Geyser
- Floodgate

These are only needed if Bedrock support is desired.

## Web Exposure

The plugin generates web assets under `plugins/BlueMapCommunityNames/web/`. Your BlueMap
web environment must make those assets reachable by the browser for the overlay to load.
How that is done is server-specific and administrator-managed.
