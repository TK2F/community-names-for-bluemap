# Security Policy

## Supported Versions

Only the latest GitHub release is supported for security fixes unless the release notes
say otherwise. Older prereleases, development snapshots, local builds, and modified
builds are not supported.

## Reporting a Vulnerability

Use GitHub private vulnerability reporting or the GitHub security advisory flow if it is
available for this repository.

If private reporting is not available, open a minimal public issue that does not include
exploit details or sensitive data. Ask for a private coordination path in that issue.

## What Not to Include in Public Reports

Do not include any of the following in public issues, pull requests, screenshots, logs,
or discussion threads:

- secrets or tokens
- webhook URLs
- server IPs or private hostnames
- private logs
- real player names
- real community names
- UUIDs
- private server configuration
- private owner or administrator contact details
- private community data

Use placeholders and keep sensitive reproduction details private until a coordination
path is agreed.

## Scope and Dependency Boundary

CommunityNames for BlueMap / BlueMapCommunityNames handles its own plugin code,
generated JSON/assets, and BlueMap browser overlay behavior.

Report vulnerabilities in BlueMap, LuckPerms, Geyser, Floodgate, Paper, nginx, reverse
proxies, TLS, firewalls, server hosting, or network configuration to the appropriate
upstream project or service owner. Those components are installed, configured, and
operated separately by server administrators.

BlueMapCommunityNames does not bundle BlueMap, LuckPerms, Geyser, or Floodgate jars.

## Response Expectations

Security reports will be reviewed on a best-effort basis. Acknowledgement and fix timing
depends on severity, reproducibility, maintainer availability, and whether the issue is
within this plugin's responsibility boundary.

Reports that are actually about upstream dependencies or server infrastructure may be
redirected to the relevant project or administrator.

## Non-Affiliation Notice

BlueMapCommunityNames is an independent plugin and is not affiliated with, endorsed by,
or supported by Mojang, Microsoft, Minecraft, BlueMap, LuckPerms, Geyser, Floodgate, or
Paper.
