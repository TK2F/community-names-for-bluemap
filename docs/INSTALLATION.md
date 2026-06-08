# Installation

BlueMapCommunityNames, published as CommunityNames for BlueMap, is installed as a normal
Paper plugin.

## Requirements

- Paper-compatible Minecraft server
- Java 25
- BlueMap installed separately
- LuckPerms installed separately

Optional:

- Geyser/Floodgate if Bedrock support is desired
- nginx or another reverse proxy only if your BlueMap web environment needs that model

## Steps

```mermaid
flowchart TD
    A["Install and verify BlueMap separately"] --> B["Install and verify LuckPerms separately"]
    B --> C["Install BlueMapCommunityNames jar"]
    C --> D["Start server and generate config.yml"]
    D --> E["Choose 0-3 LuckPerms meta fields and display mode"]
    E --> F["Run bcn status / bcn rebuild as needed"]
    F --> G{"Can browser fetch /bcn/ files?"}
    G -- "yes" --> H["Open BlueMap and verify roster overlay"]
    G -- "no" --> I["Configure an environment-specific web route<br/>optional reverse proxy example available"]
    I --> H
    H --> J["Smoke test Java player"]
    J --> K{"Bedrock support in scope?"}
    K -- "yes" --> L["Smoke test Geyser/Floodgate player"]
    K -- "no" --> M["Done"]
    L --> M
```

1. Install and verify BlueMap according to BlueMap's documentation.
2. Install and verify LuckPerms according to LuckPerms' documentation.
3. Build BlueMapCommunityNames:

```sh
./gradlew clean test build --no-daemon
```

4. Copy `build/libs/BlueMapCommunityNames-0.2.2.jar` to your server `plugins/`
   directory.
5. Start the server.
6. Confirm the plugin generated `plugins/BlueMapCommunityNames/config.yml`.
7. Configure roster fields and display mode.
8. Run `bcn status`.

If the browser cannot fetch `/bcn/overlay.js`, configure an appropriate web route for
your environment. See [Optional Reverse Proxy Example](REVERSE_PROXY_EXAMPLE.md).

## What Is Not Installed By This Plugin

BlueMapCommunityNames does not install, bundle, configure, or operate BlueMap,
LuckPerms, Geyser, Floodgate, nginx, firewalls, TLS, or DNS.
