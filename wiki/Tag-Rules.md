# Tag Rules

## Uniqueness

Two clans can never hold the same tag. The comparison uses the plain text of the tag, so colors and formatting are ignored, and it is case-insensitive. `ABC`, `abc` and a rainbow `ABC` are the same tag.

The check runs when the tag is validated and again inside the clan lock when it is saved. Two clans that confirm the same tag at the same moment cannot both succeed, the second one gets the error that the tag is taken.

A clan may save its own tag again, for example to change colors or case.

## Length

`tag.min-length` and `tag.max-length` count the characters of the plain text, formatting is not counted. The editor shows one item per letter, so the maximum is capped at 18.

## Allowed characters

`tag.allowed-characters` is a regular expression that the whole tag text has to match. The default `[A-Za-z0-9_]+` allows letters, digits and underscores. A text with spaces or symbols is rejected with a message.

## Blacklist

`tag.blacklist` lists forbidden words. A tag is rejected when its lower-cased text contains one of them, so `admin` also blocks `Admin1`. Players with `easyclantags.bypass.blacklist` skip this check.

## Colors

Every letter has its own RGB color. Players without `easyclantags.color.hex`, or on a server with `tag.allow-hex-colors: false`, can only use the 16 default colors. The check runs on the stored colors, so a gradient is rejected too when it produces other values.

## Formats

Bold, italic, underline, strikethrough and obfuscated apply to the whole tag. Each one needs `tag.formats.<name>: true` and the permission `easyclantags.format.<name>`. When an admin turns a format off in the config later, existing tags keep the data but the format is no longer rendered.

## Cooldown

`tag.change-cooldown-seconds` is the wait time between two tag changes of a clan, measured from the last saved change. `0` turns it off. Players with `easyclantags.bypass.cooldown` ignore it.

## Where the tag appears

| Place | How |
|---|---|
| Nametag | Prefix and suffix of a personal scoreboard team |
| Tab list | `playerListName` |
| Chat | A `ChatRenderer` on the `AsyncChatEvent` |

All three update immediately when a tag is created, changed, assigned or deleted, and when a player joins, quits or respawns.
