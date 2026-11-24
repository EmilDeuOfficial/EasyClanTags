# Permissions

Basic clan permissions default to `true` so every player can use the menu. Admin, bypass and the obfuscated format default to `op`. Use a permissions plugin like [LuckPerms](https://luckperms.net/) to change that.

## Permission nodes

| Permission | Grants access to | Default |
|---|---|---|
| `easyclantags.admin` | Parent node, grants every permission below | `op` |
| `easyclantags.use` | Opening the menu with `/clantag` | `true` |
| `easyclantags.toggle` | `/clantag on` and `/clantag off` | `op` |
| `easyclantags.status` | `/clantag status` | `op` |
| `easyclantags.reload` | `/clantag reload` | `op` |
| `easyclantags.create` | Creating a clan | `true` |
| `easyclantags.edit` | Creating and editing the tag of the own clan | `true` |
| `easyclantags.delete` | Disbanding the own clan | `true` |
| `easyclantags.invite` | Inviting players | `true` |
| `easyclantags.accept` | Accepting and declining invites | `true` |
| `easyclantags.leave` | Leaving a clan | `true` |
| `easyclantags.kick` | Kicking members | `true` |
| `easyclantags.transfer` | Transferring ownership | `true` |
| `easyclantags.info` | `/clantag info` | `true` |
| `easyclantags.list` | `/clantag list` | `true` |
| `easyclantags.admin.set` | `/clantag admin set` | `op` |
| `easyclantags.admin.remove` | `/clantag admin remove` | `op` |
| `easyclantags.admin.delete` | `/clantag admin delete` | `op` |
| `easyclantags.color.hex` | Hex colors and any RGB value in the editor | `true` |
| `easyclantags.gradient` | The gradient button, also needs `easyclantags.color.hex` | `true` |
| `easyclantags.format.bold` | Bold tags | `true` |
| `easyclantags.format.italic` | Italic tags | `true` |
| `easyclantags.format.underline` | Underlined tags | `true` |
| `easyclantags.format.strikethrough` | Strikethrough tags | `true` |
| `easyclantags.format.obfuscated` | Obfuscated tags | `op` |
| `easyclantags.bypass.blacklist` | Using words from `tag.blacklist` | `op` |
| `easyclantags.bypass.cooldown` | Changing the tag during the cooldown | `op` |
| `easyclantags.chat.colors` | MiniMessage formatting in chat, only when `chat.allow-player-formatting` is true | `op` |

Without `easyclantags.color.hex` a player can still use the 16 default colors from the palette. A format also has to be enabled under `tag.formats` in the config, the permission alone is not enough.

## LuckPerms examples

Grant everything to the `admin` group:

```
/lp group admin permission set easyclantags.admin true
```

Give one player obfuscated tags:

```
/lp user Steve permission set easyclantags.format.obfuscated true
```

Limit the default group to the 16 default colors:

```
/lp group default permission set easyclantags.color.hex false
```

Revoke a permission again:

```
/lp group default permission unset easyclantags.color.hex
```

## plugin.yml inheritance

```yaml
permissions:
  easyclantags.admin:
    children:
      easyclantags.toggle: true
      easyclantags.status: true
      easyclantags.reload: true
      easyclantags.admin.set: true
      easyclantags.admin.remove: true
      easyclantags.admin.delete: true
```

The real file lists every node as a child. Any Bukkit-compatible permissions plugin reads this hierarchy automatically.
