# Simple CraftAttack Plugin

With simple features to start a community survival project like the german youtuber project "CraftAttack".

(This is not an official plugin from the youtube project!)

## Commands/Features:

Command | Permission | Default | Description
-------- | -------- | -------- | --------
reload | ca.admin.reload | op | reloads config.yml
start | ca.admin.start | op | starts start animation
stop | ca.admin.stop | op | stops start animation
teleportall | ca.admin.teleportall | op | teleports all players to start position
settab | ca.admin.settab | op | sets the tab text
settitle | ca.admin.settitle | op | sets the start title shown for players
invsee | ca.admin.invsee | op | lets you see the inventory of the players
setspawn | ca.admin.setspawn | op | creates a protected spawn area where only players with ca.builder.spawn or ca.admin.spawn can build and destroy
pvp | ca.admin.pvp | op | Enables/Disables PVP
pregen | ca.admin.pregen | op | Pregenerates chunks in defined number of blocks
maintenance | ca.admin.maintenance | op | Maintenance on/off
tempban | ca.admin.temoban | op | Temporary bans players for a specified time
timeout | ca.admin.timeout | op | Temporary stops players from writing text in the chat for a specified time

Additional Features:
- Spawn protection in SpawnArea
- Spawn elytra (if enabled) beginning in SpawnArea 
- Spawn-Teleporter
- Changing MOTD
- Webserver & JSON-Api

## A deeper look into the features:

### Spawnprotection
- You can set the coordinates and radius with /ca setspawn <x> <y> <z> <radius>
- or set the edge coordinates in the config

### Spawn elytra
- can be enabled/disabled in the config
- you can enable a boost which is triggerd by the "spaw offhand" keybind
- you can change the boost multiplyValue in the config
- you can set a radius in which the boost can be activated

### Webserver and API
- you can enable/disable and change the port of both in the config

### Spawn teleporter
**IMPORTANT**: You have to restart the server for the changes to get reloaded.
- you can enable/disable the feature in the config
- you can play a sound when the player is being teleported
- you have to set the coordinates (x1, y1, z1, x2, y2, z3) in the config
- you can set a delay which the player has to wait until he gets teleported
- you have to set the coordinates where the player gets teleported to

### Changing MOTD
**IMPORTANT**: You have to restart the server for the changes to get reloaded.
- you can set different MOTDs in the config
- example: \u00a7aCraftAttack13 \u00a76community\u00a7r\n\u00a7bOfficial start!
- It is normal that after a restart the format is a bit strage.

#### API:

## API-Endpunkte Übersicht

| Base-URL           | Action (`action` Parameter)  | Beschreibung                                            | Erforderliche Parameter          |
|--------------------|------------------------------|--------------------------------------------------------|---------------------------------|
| `/api/whitelist`   | `list`                       | Gibt die Whitelist aller Spieler zurück                | keine                          |
| `/api/whitelist`   | `check`                      | Prüft, ob Spieler mit UUID auf der Whitelist ist       | `uuid`                         |
| `/api/whitelist`   | `add`                        | Fügt Spieler zur Whitelist hinzu                        | `name`                         |
| `/api/whitelist`   | `remove`                     | Entfernt Spieler von der Whitelist                      | `name`                         |
| `/api/management`  | `kick`                       | Kickt einen Spieler vom Server                           | `name`, optional `reason`      |
| `/api/management`  | `ban`                        | Bannt einen Spieler                                     | `name`, optional `reason`      |
| `/api/management`  | `unban`                      | Entbannt einen Spieler                                  | `name`                         |
| `/api/management`  | `banlist`                    | Gibt die Liste aller gebannten Spieler zurück           | keine                          |
| `/api/management`  | `restart`                    | Startet den Server neu                                  | keine                          |
| `/api/maintenance` | `status`                     | Gibt den aktuellen Wartungsmodus und Whitelist zurück  | keine                          |
| `/api/maintenance` | `set`                        | Aktiviert oder deaktiviert den Wartungsmodus            | `active` (true/false)          |
| `/api/maintenance` | `add`                        | Fügt Spieler zur Wartungs-Whitelist hinzu               | `name`                         |
| `/api/maintenance` | `remove`                     | Entfernt Spieler von der Wartungs-Whitelist             | `name`                         |

---

### Beispiel URLs

- Whitelist abrufen:  
  `http://localhost:8020/api/whitelist?action=list`

- Whitelist prüfen:  
  `http://localhost:8020/api/whitelist?action=check&uuid=<UUID>`

- Spieler zur Whitelist hinzufügen:  
  `http://localhost:8020/api/whitelist?action=add&name=Spielername`

- Spieler kicken:  
  `http://localhost:8020/api/management?action=kick&name=Spielername&reason=Grund`

- Spieler bannen:  
  `http://localhost:8020/api/management?action=ban&name=Spielername&reason=Grund`

- Wartungsmodus Status abfragen:  
  `http://localhost:8020/api/maintenance?action=status`

- Wartungsmodus aktivieren:  
  `http://localhost:8020/api/maintenance?action=set&active=true`

- Spieler zur Wartungs-Whitelist hinzufügen:  
  `http://localhost:8020/api/maintenance?action=add&name=Spielername`


## Download:

[Modrinth](https://modrinth.com/plugin/simple-craftattack#download)

[Github](https://github.com/VoidableMoon884/CraftAttack)