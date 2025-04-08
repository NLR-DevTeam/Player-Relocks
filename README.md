# Player Relocks
A simple Fabric mod for Minecraft 25w14craftmine that allows you to revoke `Player Unlock`s.

## Usage
Hold shift and left-click on an `Player Unlock` widget, and it will be revoked.

## Installation
Download from [Modrinth](https://modrinth.com/mod/player-relocks) or the releases page, and put the jar file into `mods`.

Installing this mod on server is required, too.

## Configuration
Configuration file can be discovered at `config/player-relocks.json`, currently a restart is required to apply changes.

Here are explanations of each configuration item.

### expGiveBackRatio (float, default: 0.0)
Intentionally, no experience points will be given back as a penalty mechanism.  
For instance, you can set this item to `1.0` to give back all of your experience points on re-locking.