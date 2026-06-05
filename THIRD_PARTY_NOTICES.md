# Third-Party Notices

This file summarizes third-party projects that CommunityNames for BlueMap integrates
with or targets. It is a public-facing credit and dependency-boundary document, not
legal advice.

## Dependency Boundary

CommunityNames for BlueMap / BlueMapCommunityNames does not bundle BlueMap, LuckPerms,
Geyser, Floodgate, or Paper jars. Server administrators install, configure, operate, and
review those projects separately according to their own environment and those projects'
documentation and licenses.

nginx, reverse proxies, TLS, firewalls, DNS, hosting, and network exposure are also
administrator-managed and are outside this plugin's responsibility boundary.

## Required Administrator-Installed Dependencies

### BlueMap

- Purpose: Provides the BlueMap web app and API integration target for the roster
  overlay.
- Bundled: no.
- Required: yes.
- License identified from official source: MIT license.
- Source: https://github.com/BlueMap-Minecraft/BlueMap

### LuckPerms

- Purpose: Provides the configured meta values read by this plugin.
- Bundled: no.
- Required: yes.
- License identified from official source: MIT license.
- Source: https://github.com/LuckPerms/LuckPerms

## Optional Administrator-Installed Dependencies

### Geyser

- Purpose: Optional Bedrock support environment.
- Bundled: no.
- Required: no; only relevant when Bedrock support is desired.
- License identified from official source: MIT license.
- Source: https://github.com/GeyserMC/Geyser

### Floodgate

- Purpose: Optional Floodgate identity detection for Bedrock/Floodgate environments.
- Bundled: no.
- Required: no; only relevant when Floodgate support is desired.
- License identified from official source: MIT license.
- Source: https://github.com/GeyserMC/Floodgate

## Platform / API

### Paper / Paper API

- Purpose: Paper-compatible server platform and compile-time API target.
- Bundled: no.
- Required: a Paper-compatible server is required at runtime.
- License identified from official source: Paper inherits GPLv3 licensing from included
  upstream projects, with some contributors choosing MIT licensing for their code as
  described in Paper's `LICENSE.md`.
- Source: https://github.com/PaperMC/Paper

## Non-Affiliation Notice

BlueMapCommunityNames is an independent plugin and is not affiliated with, endorsed by,
or supported by Mojang, Microsoft, Minecraft, BlueMap, LuckPerms, Geyser, or Floodgate.

## Notes

This plugin's release artifact contains its own plugin classes, configuration defaults,
and web overlay templates. It does not include third-party server/plugin jars.
