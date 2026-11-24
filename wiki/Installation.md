# Installation

## Requirements

| Requirement | Version |
|---|---|
| Server software | Paper 26.1 to 26.3, or Paper 1.21 to 1.21.11 |
| Java | 25 or higher for Paper 26.x, 21 or higher for Paper 1.21.x |

Paper 26.1 and newer need Java 25. There are two JARs, use the one that matches the server: `EasyClanTags_x.x_26.x.jar` for Paper 26.x and `EasyClanTags_x.x_1.21.x.jar` for Paper 1.21.x. Purpur and Pufferfish should work as Paper forks. Spigot and Folia are not supported.

## Step-by-step

### 1. Download the JAR

Download the latest `EasyClanTags_x.x_26.x.jar` from the [Releases page](https://github.com/EmilDeuOfficial/EasyClanTags/releases).

### 2. Place it in the plugins folder

```
server/
  plugins/
    EasyClanTags_1.0_26.x.jar
```

### 3. Start the server

On the first start the plugin creates its files:

```
plugins/
  EasyClanTags/
    config.yml
    clans.yml
    lang/
      messages_en.yml
      messages_de.yml
      messages_ru.yml
      messages_es.yml
      messages_fr.yml
      messages_ja.yml
      messages_zh.yml
```

`clans.yml` appears after the first change to a clan. With `storage.type: SQLITE` the plugin writes `clans.db` instead, using the SQLite driver that ships with Paper.

### 4. Verify

The console prints a line like this:

```
[EasyClanTags] EasyClanTags enabled. Clans: 0, storage: YAML (clans.yml)
```

## First use

Join the server and run `/clantag`. The menu offers "Create a clan". After the clan exists, the owner opens "Edit the tag" to create the tag.

## Updating

1. Stop the server.
2. Replace the old JAR with the new one.
3. Start the server.

`config.yml` and the language files in `plugins/EasyClanTags/lang/` are kept. Missing message keys fall back to the English texts bundled in the JAR. New options appear in the bundled `config.yml` only, so copy them over by hand when you want to change them.

## Uninstalling

1. Run `/clantag off`. All nametag teams and tab names are removed.
2. Stop the server.
3. Delete the JAR and the `plugins/EasyClanTags/` folder.
