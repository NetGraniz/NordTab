# NordTab

TAB header and footer for Paper 26.2 and Folia 26.2. One JAR supports both platforms on Java 25.

The default header reads NORD FJELL. The footer shows TPS, online players and the viewer's ping. Edit the installed configuration to change the text.

## Behavior

NordTab uses the Paper API. Player updates run on each player's entity scheduler, and reload publishes immutable settings.

On Folia, `<tps>` belongs to the viewer's region, not a single server-wide tick loop. The Minecraft client controls the player-list grid and ping bars; NordTab does not control that layout.

## Permissions

| Permission | Allows | Default |
| --- | --- | --- |
| `nordtab.admin` | `/nordtab reload` | Operators |

Players do not need a NordTab permission to see the header and footer.

## Configuration and installation

Keep `plugins/NordTab/config.yml` when updating. Run `/nordtab reload` after editing its text.

Use a stopped-server installation or removal, not hot loading or unloading. Both platforms use the release from `main`.

## Build

Run `./build.ps1` with Maven 3.9+ and JDK 25. The output is `target/NordTab-1.1.0.jar`. See [BUILDING.md](BUILDING.md).
