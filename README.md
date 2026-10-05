# NordTab

> Release build and installation requirements: see [BUILDING.md](BUILDING.md).
> Older local paths below describe historical test fixtures, not the release build.

Lightweight Paper player list for Nord Fjell. It uses only the Paper API and does not require TAB, PlaceholderAPI or PacketEvents.

The header shows `NORD FJELL`. The footer shows current TPS, online count and the viewing player's ping. Minecraft clients control the player grid and connection bars, so the plugin deliberately does not use fragile packet tricks to imitate the exact 2b2t column layout.

Build with `./build.ps1`. Install the resulting `build/NordTab-1.0.0.jar` while the server is stopped, then start the server and edit `plugins/NordTab/config.yml` if required.
