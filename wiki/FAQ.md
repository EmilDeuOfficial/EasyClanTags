# FAQ

### Which Minecraft versions are supported?

Paper 26.1 to 26.3 with Java 25 (`EasyClanTags_x.x_26.x.jar`) and Paper 1.21 to 1.21.11 with Java 21 (`EasyClanTags_x.x_1.21.x.jar`). Paper changed its version scheme with 26.1. The 26.x JAR declares `api-version: '26.1'` and was compiled against 26.1.2 and 26.3, the 1.21 JAR declares `api-version: '1.21'` and was compiled against 1.21.11. Both JARs have the same features, only the build target differs. Servers older than 1.21 are not supported.

### The tag does not show above the head.

1. Check that `areas.nametag` is `true` and `/ct status` reports the state on.
2. Look at the console for a warning like "is in the scoreboard team ... from another plugin". The plugin does not take players out of teams that belong to other plugins.
3. Check the `nametag.prefix` format. It has to contain `<tag>` or `<tag_leading>`.

### The tag does not show in chat.

Another chat plugin may replace the renderer after EasyClanTags. Either use the chat format of that plugin and add the tag through a placeholder, or disable it. Check that `areas.chat` is `true`.

### Can two clans use the same tag in different colors or case?

No. The comparison ignores colors, formatting and case.

### Can I use MiniMessage in the formats?

Yes. `chat.format`, `nametag.prefix`, `nametag.suffix` and `tab.format` are MiniMessage strings. Available placeholders are `<tag>`, `<tag_leading>`, `<tag_bare>`, `<clan>`, `<player>`, and in chat `<displayname>` and `<message>`.

### How do I put the tag behind the name?

Use `nametag.suffix: "<tag_leading>"` and an empty `nametag.prefix`. In the tab list use `tab.format: "<player><tag_leading>"`.

### How does the tab list ordering work?

With `sorting.mode: CLAN` the plugin sorts clans by their tag text and players without a clan go last (or first, with `sorting.clanless: FIRST`). Inside one clan the order follows the first letters of the player names. `NAME` sorts by player name only. Set `sorting.enabled: false` to keep the vanilla order.

### Does it work with other scoreboard plugins?

Yes, as long as they do not put the same players into their own teams. The plugin registers its teams on the main scoreboard and on every custom scoreboard that online players use. When a player already sits in a team from another plugin, the plugin skips that player and logs one warning.

### What happens when I run /clantag off?

All scoreboard teams of the plugin are removed, tab names are reset and the chat format returns to the default. Clans and tags stay saved. `/clantag on` restores everything.

### Can I edit config.yml while the server is running?

Yes, run `/clantag reload` afterwards. Menus that are open are closed.

### How do I switch from YAML to SQLite?

Set `storage.type: SQLITE` and run `/clantag reload`. The current clans are written to `clans.db`. The plugin uses the SQLite driver that ships with Paper. When the driver is not available the plugin logs an error and keeps using YAML.

### The plugin disabled itself on start.

The console shows why. The usual cause is a `clans.yml` or `clans.db` that cannot be read. The plugin disables itself instead of overwriting data it could not read. Fix or move the file and restart.

### Does it work with Purpur or Pufferfish?

They should, because both keep the Paper API. They were not tested.

### Does it work with Folia?

No. The plugin uses scoreboard teams on the main thread and does not declare `folia-supported`.

### Is PlaceholderAPI supported?

Not in version 1.0. The plugin has its own placeholders in the formats and does not depend on it.
