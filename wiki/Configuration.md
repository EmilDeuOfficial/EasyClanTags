# Configuration

The config file is `plugins/EasyClanTags/config.yml`. It is created with default values on the first start. Apply changes without a restart with `/clantag reload`. Language files are in `plugins/EasyClanTags/lang/` and can be edited the same way.

## Full default config

```yaml
# EasyClanTags configuration
# Apply changes without a restart with /clantag reload.
# All text options use MiniMessage: https://docs.advntr.dev/minimessage/format.html

# Language of messages and menus: en, de, ru, es, fr, ja, zh
language: en

# Master switch. Also changed by /clantag on and /clantag off.
global-enabled: true

# Where clans and tags are stored.
# type: YAML or SQLITE. SQLITE uses the sqlite driver that ships with Paper and falls back to YAML when it is missing.
# All writes run on a separate thread, the main thread is never blocked.
storage:
  type: YAML
  yaml-file: clans.yml
  sqlite-file: clans.db

# Turn each place where the tag is shown on or off.
areas:
  chat: true
  tab: true
  nametag: true

# Placeholders for chat.format, nametag.prefix, nametag.suffix and tab.format:
#   <tag>          brackets and tag followed by the separator, empty without a tag
#   <tag_leading>  separator followed by brackets and tag, empty without a tag (for suffixes)
#   <tag_bare>     only the colored letters, no brackets, no separator
#   <clan>         clan name
#   <player>       player name
#   <displayname>  display name of the player (chat only, equals <player> elsewhere)
#   <message>      chat message (chat only)
chat:
  format: "<tag><displayname><dark_gray> > <white><message>"
  # Allow MiniMessage formatting in chat messages of players with easyclantags.chat.colors.
  allow-player-formatting: false

nametag:
  prefix: "<tag>"
  suffix: ""

tab:
  format: "<tag><player>"

# Tab list and nametag team ordering. Needs scoreboard teams, so it stays active even
# when areas.nametag is off.
#   enabled: false keeps the vanilla order by player name
#   mode: CLAN sorts by tag text, NAME sorts by player name
#   clanless: LAST or FIRST, where players without a clan are placed in CLAN mode
sorting:
  enabled: true
  mode: CLAN
  clanless: LAST

tag:
  # Length of the tag text without formatting. max-length is capped at 18 by the editor.
  min-length: 2
  max-length: 6
  # Regular expression the whole tag text has to match.
  allowed-characters: "[A-Za-z0-9_]+"
  # Case-insensitive. A tag is rejected when it contains one of these words.
  # Players with easyclantags.bypass.blacklist ignore this list.
  blacklist:
    - admin
    - moderator
    - owner
    - staff
  # Brackets around the tag. color is a hex code like #AAAAAA or a name like gray.
  brackets:
    open: "["
    close: "]"
    color: "#AAAAAA"
  # Text between the tag and the player name.
  separator: " "
  # Color used for letters that were not colored yet.
  default-color: "#FFFFFF"
  # Seconds a clan has to wait between tag changes. 0 disables the cooldown.
  # Players with easyclantags.bypass.cooldown ignore it.
  change-cooldown-seconds: 300
  # false limits tags to the 16 default colors, true needs easyclantags.color.hex for hex colors.
  allow-hex-colors: true
  # Gradient button in the tag editor, needs easyclantags.gradient.
  allow-gradient: true
  # Formatting that can be used in tags, each also needs easyclantags.format.<name>.
  formats:
    bold: true
    italic: true
    underline: true
    strikethrough: true
    obfuscated: false

clan:
  min-name-length: 3
  max-name-length: 16
  # Regular expression for clan names.
  name-pattern: "[A-Za-z0-9_]+"
  # Maximum members including the owner. The member menu shows up to 45.
  max-members: 20
  invite-timeout-seconds: 60

gui:
  # Seconds a player has to type text into chat before the prompt is cancelled.
  input-timeout-seconds: 60
  # Every item has a material and a slot. name is optional MiniMessage that replaces
  # the name from the language file when it is not empty. A slot of -1 means the menu
  # places the item itself. The menus have 27 slots (main menu, member actions, dialogs)
  # or 54 slots (member list, invites, tag editor).
  items:
    filler: { material: GRAY_STAINED_GLASS_PANE, slot: -1, name: "" }
    info: { material: BOOK, slot: 4, name: "" }
    create-clan: { material: EMERALD, slot: 10, name: "" }
    edit-tag: { material: NAME_TAG, slot: 10, name: "" }
    members: { material: PLAYER_HEAD, slot: 12, name: "" }
    preview: { material: SPYGLASS, slot: 14, name: "" }
    invites: { material: WRITABLE_BOOK, slot: 16, name: "" }
    leave: { material: OAK_DOOR, slot: 22, name: "" }
    disband: { material: TNT, slot: 22, name: "" }
    back: { material: ARROW, slot: 45, name: "" }
    back-small: { material: ARROW, slot: 22, name: "" }
    dialog-confirm: { material: LIME_CONCRETE, slot: 11, name: "" }
    dialog-cancel: { material: RED_CONCRETE, slot: 15, name: "" }
    invite-entry: { material: PAPER, slot: -1, name: "" }
    confirm: { material: LIME_CONCRETE, slot: 53, name: "" }
    cancel: { material: RED_CONCRETE, slot: 45, name: "" }
    prev-page: { material: ARROW, slot: 48, name: "" }
    next-page: { material: ARROW, slot: 50, name: "" }
    invite-player: { material: EMERALD, slot: 49, name: "" }
    member: { material: PLAYER_HEAD, slot: -1, name: "" }
    kick: { material: IRON_BOOTS, slot: 11, name: "" }
    transfer: { material: GOLDEN_HELMET, slot: 15, name: "" }
    letter: { material: PAPER, slot: -1, name: "" }
    hex-color: { material: PAINTING, slot: 26, name: "" }
    apply-all: { material: BUCKET, slot: 35, name: "" }
    gradient: { material: AMETHYST_SHARD, slot: 42, name: "" }
    format-bold: { material: IRON_INGOT, slot: 36, name: "" }
    format-italic: { material: GOLD_INGOT, slot: 37, name: "" }
    format-underline: { material: COPPER_INGOT, slot: 38, name: "" }
    format-strikethrough: { material: NETHERITE_INGOT, slot: 39, name: "" }
    format-obfuscated: { material: ENDER_EYE, slot: 40, name: "" }
    change-text: { material: ANVIL, slot: 47, name: "" }
    editor-preview: { material: OAK_SIGN, slot: 49, name: "" }
  # Slots of the 16 default colors in the tag editor, in the order of the Minecraft color codes
  # (black, dark blue, dark green, dark aqua, dark red, dark purple, gold, gray, dark gray,
  # blue, green, aqua, red, light purple, yellow, white).
  palette-slots: [18, 19, 20, 21, 22, 23, 24, 25, 27, 28, 29, 30, 31, 32, 33, 34]
```

## Option reference

### language

| | |
|---|---|
| Type | `string` |
| Default | `en` |
| Options | `en`, `de`, `ru`, `es`, `fr`, `ja`, `zh` |

Language of messages and menus. Keys missing from a language file fall back to English.

### global-enabled

| | |
|---|---|
| Type | `boolean` |
| Default | `true` |
| Command | `/clantag on` and `/clantag off` |

Master switch. When `false`, no teams, tab names or chat formats are applied. Clans and tags are not touched.

### storage

| | |
|---|---|
| `type` | `YAML` or `SQLITE`, default `YAML` |
| `yaml-file` | File name inside the plugin folder, default `clans.yml` |
| `sqlite-file` | File name inside the plugin folder, default `clans.db` |

Saving runs on a separate thread and writes a full snapshot after every change. The final snapshot is written on shutdown. When the stored data cannot be read at start, the plugin disables itself so nothing is overwritten.

### areas

`chat`, `tab` and `nametag` switch each place on or off. All default to `true`.

### chat, nametag, tab

These options are MiniMessage strings. Placeholders:

| Placeholder | Meaning |
|---|---|
| `<tag>` | Brackets and tag followed by `tag.separator`, empty without a tag |
| `<tag_leading>` | `tag.separator` followed by brackets and tag, empty without a tag |
| `<tag_bare>` | Only the colored letters |
| `<clan>` | Clan name |
| `<player>` | Player name |
| `<displayname>` | Display name, chat only |
| `<message>` | Chat message, chat only |

`chat.allow-player-formatting` lets players with `easyclantags.chat.colors` use MiniMessage in their messages. It is `false` by default.

### sorting

| | |
|---|---|
| `enabled` | `true` orders the tab list through scoreboard teams, `false` keeps the vanilla order |
| `mode` | `CLAN` sorts by tag text, `NAME` sorts by player name |
| `clanless` | `LAST` or `FIRST`, where players without a clan go in `CLAN` mode |

The team name starts with a sort key. In `CLAN` mode the key is the rank of the clan tag followed by two letters of the player name. Sorting teams exist even when `areas.nametag` is `false`.

### tag

| Option | Default | Description |
|---|---|---|
| `min-length`, `max-length` | `2`, `6` | Length of the plain text, the maximum is capped at 18 |
| `allowed-characters` | `[A-Za-z0-9_]+` | Regular expression the whole text has to match |
| `blacklist` | `admin`, `moderator`, `owner`, `staff` | Forbidden words, case-insensitive, matched inside the text |
| `brackets.open`, `brackets.close` | `[`, `]` | Characters around the tag |
| `brackets.color` | `#AAAAAA` | Hex code or a color name like `gray` |
| `separator` | one space | Text between tag and player name |
| `default-color` | `#FFFFFF` | Color of letters that have not been colored |
| `change-cooldown-seconds` | `300` | Wait time between tag changes, `0` disables it |
| `allow-hex-colors` | `true` | `false` limits tags to the 16 default colors |
| `allow-gradient` | `true` | Enables the gradient button |
| `formats.*` | all `true` except `obfuscated` | Formats players may use, each also needs a permission |

### clan

| Option | Default | Description |
|---|---|---|
| `min-name-length`, `max-name-length` | `3`, `16` | Length of clan names |
| `name-pattern` | `[A-Za-z0-9_]+` | Regular expression for clan names |
| `max-members` | `20` | Members including the owner, capped at 45 by the member menu |
| `invite-timeout-seconds` | `60` | How long an invite stays valid |

### gui

`input-timeout-seconds` is the time a player has for a chat prompt. `items` lists every menu item with `material`, `slot` and an optional `name` that replaces the name from the language file. A slot of `-1` lets the menu place the item. `palette-slots` holds the 16 slots of the default colors in the tag editor. Set `filler` to `AIR` to leave empty slots empty.
