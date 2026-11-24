# EasyClanTags

Clan tags with one color per letter, shown above the head, in the tab list and in chat. Players build their tag in an inventory menu with a live preview, and no two clans can hold the same tag.

The plugin runs on its own. It needs no TAB, chat plugin, Vault or PlaceholderAPI, and it uses only the Paper and Adventure API (no NMS, no packets).

## Features

- Tag shown in the nametag (one scoreboard team per player), in the tab list (`playerListName`) and in chat (`AsyncChatEvent` with its own `ChatRenderer`)
- Each of the three places can be switched on or off, and each format is a MiniMessage string with placeholders
- Every letter has its own color, full RGB palette with hex codes, plus a gradient between two colors that is calculated with the MiniMessage `<gradient>` tag
- Bold, italic, underline, strikethrough and obfuscated per tag, each limited by config and by permission
- Unique tags: the comparison uses the plain text, ignores colors and formatting and is case-insensitive, so `ABC`, `abc` and a rainbow `ABC` count as the same tag
- Minimum and maximum length, an allowed-characters regex and a word blacklist
- Basic clan system: create, invite, accept, leave, kick, transfer ownership, disband, with a member limit and an invite timeout
- Inventory menus for everything: main menu, tag editor, member management, invites. All titles, item names and lore come from the language files
- Tag editor with one item per letter, a 16 color palette, hex input, "same color for all", gradient, format toggles and a live preview of nametag, tab list and chat
- Menu protection against item theft, shift click, drag, number keys and hotbar swaps
- Cooldown for tag changes and a bypass permission
- Tab list and nametag ordering by clan tag or by player name
- Storage in YAML or SQLite, written on a separate thread
- Existing scoreboard teams of other plugins are left alone and a warning is printed to the console
- Languages: en, de, ru, es, fr, ja, zh

## Configuration

```yaml
language: en
global-enabled: true

storage:
  type: YAML        # or SQLITE
  yaml-file: clans.yml
  sqlite-file: clans.db

areas:
  chat: true
  tab: true
  nametag: true

chat:
  format: "<tag><displayname><dark_gray> > <white><message>"
  allow-player-formatting: false

nametag:
  prefix: "<tag>"
  suffix: ""

tab:
  format: "<tag><player>"

sorting:
  enabled: true
  mode: CLAN        # or NAME
  clanless: LAST    # or FIRST

tag:
  min-length: 2
  max-length: 6
  allowed-characters: "[A-Za-z0-9_]+"
  blacklist: [admin, moderator, owner, staff]
  brackets:
    open: "["
    close: "]"
    color: "#AAAAAA"
  separator: " "
  change-cooldown-seconds: 300
  allow-hex-colors: true
  allow-gradient: true
  formats:
    bold: true
    italic: true
    underline: true
    strikethrough: true
    obfuscated: false

clan:
  max-members: 20
  invite-timeout-seconds: 60
```

The full file with comments also contains the clan name rules, the input timeout and the material, slot and name of every menu item.

## Permissions

| Permission | Description | Default |
|---|---|---|
| `easyclantags.admin` | Parent node that grants every permission below | `op` |
| `easyclantags.use` | Open the menu with `/clantag` | `true` |
| `easyclantags.toggle` | Use `/clantag on` and `/clantag off` | `op` |
| `easyclantags.status` | Use `/clantag status` | `op` |
| `easyclantags.reload` | Use `/clantag reload` | `op` |
| `easyclantags.create` | Create a clan | `true` |
| `easyclantags.edit` | Create and edit the own clan tag | `true` |
| `easyclantags.delete` | Disband the own clan | `true` |
| `easyclantags.invite` | Invite players | `true` |
| `easyclantags.accept` | Accept or decline invites | `true` |
| `easyclantags.leave` | Leave the clan | `true` |
| `easyclantags.kick` | Kick members | `true` |
| `easyclantags.transfer` | Transfer ownership | `true` |
| `easyclantags.info` | Use `/clantag info` | `true` |
| `easyclantags.list` | Use `/clantag list` | `true` |
| `easyclantags.admin.set` | Use `/clantag admin set` | `op` |
| `easyclantags.admin.remove` | Use `/clantag admin remove` | `op` |
| `easyclantags.admin.delete` | Use `/clantag admin delete` | `op` |
| `easyclantags.color.hex` | Use hex colors, not only the 16 default colors | `true` |
| `easyclantags.gradient` | Use gradients | `true` |
| `easyclantags.format.bold` | Use bold in a tag | `true` |
| `easyclantags.format.italic` | Use italic in a tag | `true` |
| `easyclantags.format.underline` | Use underline in a tag | `true` |
| `easyclantags.format.strikethrough` | Use strikethrough in a tag | `true` |
| `easyclantags.format.obfuscated` | Use obfuscated text in a tag | `op` |
| `easyclantags.bypass.blacklist` | Use words from the blacklist | `op` |
| `easyclantags.bypass.cooldown` | Ignore the tag change cooldown | `op` |
| `easyclantags.chat.colors` | Use MiniMessage in chat (needs `chat.allow-player-formatting`) | `op` |

LuckPerms examples:

```
/lp group admin permission set easyclantags.admin true
/lp group vip permission set easyclantags.format.obfuscated true
/lp group default permission set easyclantags.color.hex false
```

## Commands

| Command | Description |
|---|---|
| `/clantag` | Open the main menu |
| `/clantag on` | Enable the plugin globally |
| `/clantag off` | Disable the plugin globally, no data is removed |
| `/clantag status` | Show state, clan count, storage and language |
| `/clantag reload` | Reload `config.yml` and the language files and re-apply all displays |
| `/clantag create <name>` | Create a clan |
| `/clantag delete confirm` | Disband your clan |
| `/clantag invite <player>` | Invite an online player |
| `/clantag accept [clan]` | Accept an invite |
| `/clantag deny [clan]` | Decline an invite |
| `/clantag leave` | Leave your clan |
| `/clantag kick <player>` | Remove a member |
| `/clantag transfer <player>` | Make another member the owner |
| `/clantag info [clan or tag]` | Show clan details |
| `/clantag list` | List all clans |
| `/clantag admin set <player> <tag>` | Give the clan of a player a tag, a clan is created if the player has none |
| `/clantag admin remove <player>` | Remove the tag of the clan of a player |
| `/clantag admin delete <tag>` | Delete the clan that uses a tag |

Alias: `/ct`. All subcommands have tab completion.

## Requirements

| | |
|---|---|
| Server | Paper 26.1 to 26.3 with `EasyClanTags_1.0_26.x.jar`, Paper 1.21 to 1.21.11 with `EasyClanTags_1.0_1.21.x.jar` (Purpur and Pufferfish should work as Paper forks) |
| Java | 25+ for Paper 26.x, 21+ for Paper 1.21.x |

Spigot and Folia are not supported. SQLite storage uses the driver that ships with Paper and falls back to YAML when it is missing.

## Installation

1. Copy the JAR that matches your server into the `plugins/` folder: `EasyClanTags_1.0_26.x.jar` for Paper 26.x or `EasyClanTags_1.0_1.21.x.jar` for Paper 1.21.x.
2. Start the server. `config.yml`, the language files and `clans.yml` are created in `plugins/EasyClanTags/`.
3. Run `/clantag` in game, create a clan and open the tag editor.

## Building

```
mvn package
```

The JAR is written to `target/EasyClanTags-1.0.jar`. Maven needs a JDK 25 or newer. The default build targets Paper 26.x with Java 25. For Paper 1.21.x use the profile `paper-1.21`, which compiles against Paper API 1.21.11 for Java 21 and sets `api-version: '1.21'`:

```
mvn package -Ppaper-1.21
```

The Paper API version is set by the `paper.version` property in `pom.xml`.

## How it works

The tag is stored as a list of letters with an RGB value each, plus a set of formats. The gradient button builds a `<gradient:start:end>` string, lets MiniMessage render it and reads the color of every letter back, so the result matches the MiniMessage gradient exactly. Rendering walks the letters once and produces an Adventure `Component`, which is inserted into the MiniMessage formats through the `<tag>` placeholders. The uniqueness check compares the lower-cased plain text and runs again under the clan lock when the tag is saved, so two clans confirming the same tag at the same moment cannot both win.

Every online player with a clan gets a personal scoreboard team named `ect` + sort key + 8 hex digits of the UUID. The team prefix and suffix carry the nametag, the team name drives the tab list order, and `playerListName` carries the tab text. Teams are registered on the main scoreboard and on every custom scoreboard that online players use. A player who already sits in a team from another plugin is skipped and a warning is logged once. Teams that belong to the plugin are removed on shutdown, on `/clantag off` and on reload.

All clan state lives in concurrent maps, mutations run under one lock and every change creates a new immutable clan record. Saving is requested after a change and executed by a single storage thread that writes a snapshot, so the main thread never waits for disk or database access. On shutdown the last snapshot is written before the thread stops.

## Source

[github.com/EmilDeuOfficial/EasyClanTags](https://github.com/EmilDeuOfficial/EasyClanTags)
