# Commands

Main command: `/clantag`
Alias: `/ct`

`/ct on`, `/ct off`, `/ct status`, `/ct reload`, `/ct list`, `/ct info <clan>` and the admin subcommands work from the console. Every other subcommand needs a player.

## Overview

| Command | Permission | Description |
|---|---|---|
| `/ct` | `easyclantags.use` | Open the main menu |
| `/ct on` | `easyclantags.toggle` | Enable the plugin globally |
| `/ct off` | `easyclantags.toggle` | Disable the plugin globally |
| `/ct status` | `easyclantags.status` | Show the current state |
| `/ct reload` | `easyclantags.reload` | Reload config and language files |
| `/ct create <name>` | `easyclantags.create` | Create a clan |
| `/ct delete confirm` | `easyclantags.delete` | Disband your clan |
| `/ct invite <player>` | `easyclantags.invite` | Invite an online player |
| `/ct accept [clan]` | `easyclantags.accept` | Accept an invite |
| `/ct deny [clan]` | `easyclantags.accept` | Decline an invite |
| `/ct leave` | `easyclantags.leave` | Leave your clan |
| `/ct kick <player>` | `easyclantags.kick` | Remove a member |
| `/ct transfer <player>` | `easyclantags.transfer` | Make another member the owner |
| `/ct info [clan or tag]` | `easyclantags.info` | Show clan details |
| `/ct list` | `easyclantags.list` | List all clans |
| `/ct admin set <player> <tag>` | `easyclantags.admin.set` | Set a tag for the clan of a player |
| `/ct admin remove <player>` | `easyclantags.admin.remove` | Remove the tag of the clan of a player |
| `/ct admin delete <tag>` | `easyclantags.admin.delete` | Delete the clan that uses a tag |

The tag itself is created and colored in the menu, there is no command for it.

## /ct on

Enables EasyClanTags globally and applies all nametags, tab names and chat formats again. The state is saved in `config.yml` as `global-enabled: true`.

## /ct off

Disables EasyClanTags globally. All scoreboard teams of the plugin are removed, tab names are reset and the chat format returns to the default. Clans and tags stay saved, `/ct on` brings everything back. The state is saved as `global-enabled: false`.

## /ct status

```
EasyClanTags status
State: on
Clans: 4, players with a tag: 11
Storage: YAML (clans.yml), language: en
```

## /ct reload

Reloads `config.yml` and the language files, closes open menus and re-applies every display. When the storage type or file name changed, the plugin saves to the old storage, switches to the new one and writes the current clans there.

## /ct create

```
/ct create Red_Team
```

Creates a clan with you as the owner. The name must match `clan.name-pattern` and the length limits, and it must not be used by another clan (case-insensitive). The clan has no tag until you create one in the menu.

## /ct delete

```
/ct delete confirm
```

Disbands the clan for all members and frees its tag. Only the owner can do this. Without `confirm` the plugin only prints a reminder.

## /ct invite, accept, deny

```
/ct invite Steve
/ct accept Red_Team
/ct deny Red_Team
```

Only the owner can invite, and only online players without a clan. An invite lasts `clan.invite-timeout-seconds`. A player with several open invites has to add the clan name to `accept` or `deny`. Invites are also listed in the main menu.

## /ct leave, kick, transfer

```
/ct leave
/ct kick Steve
/ct transfer Alex
```

The owner cannot leave, they have to transfer the clan or disband it. Kick and transfer are owner only. After a transfer the old owner becomes a normal member.

## /ct info and /ct list

```
/ct info
/ct info Red_Team
/ct info ABC
```

`info` without an argument shows your own clan, with an argument it searches clan names first and tags second. `list` prints every clan with its tag and member count.

## /ct admin

```
/ct admin set Steve ABC
/ct admin remove Steve
/ct admin delete ABC
```

`set` gives the clan of the player a plain white tag. When the player has no clan, a clan named after the tag is created with the player as owner. The tag still has to follow the length, character and uniqueness rules, the blacklist and the cooldown are skipped. The player has to be online or known to the server. `remove` clears the tag of the clan of the player. `delete` deletes the clan that uses the tag.
