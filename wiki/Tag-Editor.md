# Tag Editor

The tag editor is an inventory menu with 54 slots. It opens from the main menu through "Edit the tag" and is available to the clan owner.

## Entering the text

A new tag starts with a chat prompt. Type the text into the chat, the message is not sent to other players. Type `cancel` to abort. The prompt ends after `gui.input-timeout-seconds`. The text has to pass the length, character and blacklist rules before the editor opens.

The text is entered through the chat because it works the same on every Paper 26.x server. Paper offers no anvil input without NMS or packets, which this plugin does not use.

## Layout

| Slots | Content |
|---|---|
| 0 to 17 | One item per letter, up to 18 letters |
| 18 to 25 and 27 to 34 | The 16 default colors |
| 26 | Custom hex color |
| 35 | Same color for all letters |
| 36 to 40 | Format toggles: bold, italic, underline, strikethrough, obfuscated |
| 42 | Gradient |
| 45 | Cancel |
| 47 | Change the text |
| 49 | Live preview |
| 53 | Confirm |

All slots, materials and names can be changed in `gui.items` and `gui.palette-slots`.

## Coloring letters

1. Click a letter item. The selected letter has a glow.
2. Click a color in the palette, or the hex item and type a code like `#FF8800` or `#F80` into the chat.
3. The letter changes color and the preview updates.

The hex item needs `easyclantags.color.hex` and `tag.allow-hex-colors: true`.

## Same color and gradients

The "same color for all letters" button copies the color of the selected letter to every letter.

The gradient button works in two steps. Click it, pick the start color from the palette or through hex input, then pick the end color. The colors in between come from the MiniMessage `<gradient>` tag, which spreads the two colors over all letters. Click the gradient button again to abort. Gradients need `tag.allow-gradient: true`, `easyclantags.gradient` and `easyclantags.color.hex`.

## Formats

The five format items toggle bold, italic, underline, strikethrough and obfuscated for the whole tag. A toggle works when the format is enabled in `tag.formats` and the player has `easyclantags.format.<name>`.

## Live preview

The preview item shows how the tag looks in the nametag, the tab list and chat, using your own name and the formats from `config.yml`. A place that is switched off under `areas` shows as off.

## Confirming

The confirm button checks length, characters, blacklist, formats, colors and uniqueness again and then saves the tag. When a check fails, the editor stays open and the chat shows the reason, for example that the tag is already used by another clan. When the cooldown is active, the editor does not open at all.

Closing the menu with Escape discards the changes.
