# EasyClanTags Wiki

EasyClanTags gives clans a tag that is shown above the head, in the tab list and in chat. Each letter of the tag can have its own color, and the tag is built in an inventory menu with a live preview.

## What is EasyClanTags?

The plugin combines a small clan system with a tag editor. A clan owner picks the tag text, colors every letter and confirms. The plugin checks the tag against the length, character and blacklist rules, makes sure no other clan uses the same text, and then updates the nametag, tab list and chat of all clan members at once.

- No other plugin is required, no TAB, chat plugin, Vault or PlaceholderAPI
- Only the Paper and Adventure API is used, no NMS and no packets
- Tags are unique, the check ignores colors, formatting and case
- Data is stored in YAML or SQLite on a separate thread

## Quick Start

1. Copy `EasyClanTags_1.0_26.x.jar` (Paper 26.x) or `EasyClanTags_1.0_1.21.x.jar` (Paper 1.21.x) into your `plugins/` folder.
2. Start the server.
3. Run `/clantag` and click "Create a clan", then type the clan name into the chat.
4. Click "Edit the tag", type the tag text, color the letters and press the confirm button.
5. The tag now shows in the nametag, the tab list and chat.

## Wiki Pages

| Page | Description |
|---|---|
| [Installation](Installation) | Requirements, install steps, updating |
| [Commands](Commands) | Every `/clantag` subcommand with examples |
| [Permissions](Permissions) | Permission nodes and LuckPerms examples |
| [Configuration](Configuration) | Every `config.yml` option explained |
| [Tag-Editor](Tag-Editor) | How the inventory editor works |
| [Tag-Rules](Tag-Rules) | Uniqueness, length, characters, blacklist, colors and formats |
| [FAQ](FAQ) | Common questions and troubleshooting |

## Compatibility

| | |
|---|---|
| Server software | Paper 26.1 to 26.3 or Paper 1.21 to 1.21.11 (Purpur and Pufferfish should work as Paper forks) |
| Java | 25+ for Paper 26.x, 21+ for Paper 1.21.x |
| Permission plugins | LuckPerms or any Bukkit-compatible system |

Spigot and Folia are not supported.
