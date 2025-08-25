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
- Webserver & JSON-Api

### Api:
Running on port 8020

http://localhost:8020/api/COMMAND?action=ACTION&addition=...

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

### Download:
[Modrinth](https://modrinth.com/plugin/simple-craftattack#download)

[Github](https://github.com/VoidableMoon884/CraftAttack)