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

**URL**: http://localhost:8020/api/COMMAND?action=ACTION&addition=...

COMMAND:
- whitelist
- management

ACTION: whiteist:
action|addition|function
--------|--------|--------
list|-|list of all whitelisted players
check|uuid|checks with the player UUID if the player is on the whitelist
add|name|adds players to whitelist
remove|name|removes player from the whitelist

Examples:
URL|OUTPUT
--------|--------
http://localhost:8020/api/whitelist?action=list|{"uuid":"8f1ab746-93e2-4072-b676-81f2f8dc4e44","name":"VoidableMoon884"}
http://localhost:8020/api/whitelist?action=check&uuid=8f1ab746-93e2-4072-b676-81f2f8dc4e44|{"status":"ok","whitelisted":true,"name":"VoidableMoon884"}
http://localhost:8020/api/whitelist?action=add&name=VoidableMoon884|{"status":"ok","message":"Spieler hinzugefügt"}
http://localhost:8020/api/whitelist?action=remove&name=VoidableMoon884|{"status":"ok","message":"Spieler entfernt"}

ACTION: management
action|addition|function
--------|--------|--------
kick|name, reason|kicks a player from the world
ban|name, reason|bans a player from the server with the specified reason
unban|name|unbans a player
banlist|-|gives a list of banned players

Examples:
URL|OUTPUT
--------|--------
http://localhost:8020/api/management?action=ban&name=VoidableMoon884&reason=REASON|{"status":"ok","message":"Spieler VoidableMoon884 wurde gebannt: REASON"}
http://localhost:8020/api/management?action=unban&name=VoidableMoon884|{"status":"ok","message":"Spieler VoidableMoon884 wurde entbannt."}
http://localhost:8020/api/management?action=kick&name=VoidableMoon884|{"status":"error","message":"Spieler VoidableMoon884 ist nicht online."}


## Download:

[Modrinth](https://modrinth.com/plugin/simple-craftattack#download)

[Github](https://github.com/VoidableMoon884/CraftAttack)