# Compatibility

## Tested Scope

BlueMapCommunityNames, published as CommunityNames for BlueMap, has been validated with:

- Paper 26.1.2
- Java 25
- BlueMap 5.20
- LuckPerms 5.5.x
- Geyser/Floodgate for optional Bedrock smoke testing

Optional same-origin `/bcn/` routing was also tested in a local environment. This is not
a requirement and does not mean every nginx or reverse-proxy topology is supported by
this plugin.

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
