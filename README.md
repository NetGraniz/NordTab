# NordTab

> Release build and installation requirements: see [BUILDING.md](BUILDING.md).
> Older local paths below describe historical test fixtures, not the release build.

Lightweight Paper/Folia player list for Nord Fjell. It uses only the Paper API and does not require TAB, PlaceholderAPI or PacketEvents.

The header shows `NORD FJELL`. The footer shows current TPS, online count and the viewing player's ping. Minecraft clients control the player grid and connection bars, so the plugin deliberately does not use fragile packet tricks to imitate the exact 2b2t column layout.

The same JAR supports both Paper and Folia; development stays on `main`.
Updates use each player's entity scheduler, and reloads publish immutable settings.
On Folia, `<tps>` describes the viewer's current region, not a single server-wide TPS.
Use a server restart for installation/removal; hot plugin unloading is not supported.

Build with `./build.ps1`. Install `target/NordTab-1.1.0.jar` while the server is stopped.
Existing `plugins/NordTab/config.yml` remains compatible; do not overwrite live configuration with defaults.
