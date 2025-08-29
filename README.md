# Add loot to all your chests
Wanted to generate loot inside chests without having to fill them manually? This mod provides easy commands to do so, and also comes with some loottables by default.

**This mod works with all mods as long as they provide a valid loottable. See below if you don't know what that is.**

**This mod only supports normal chests, not barrels, etc (This is something I'm working on).**

## Commands
To generate loot in a certain radius, use this command:

```
/reloot generateNewLoot <loottable> [radius] [mode] [refill] [time in seconds]
```
To generate loot in all loaded chunks, use this command:

```
/reloot regenerateAllLoot <loottable> [mode] [refill] [time in seconds]
```
- **<loottable>** represents the loottable to fill the chests with.
- **[radius]** only chests within the radius (default is 50 blocks) are affected.
- **[mode]** can either be replace or keep (optional, default is keep). If set to keep, the chest keeps items already inside the chests, and if set to replace, it replaces existing loot.
- **[refill]** optional; disabled by default. If refill is set, you also need to set the time for the refill to happen. This will refill the chest after said time and repeat until the world is stopped.
- **time in seconds** The time in seconds until a refill happens.

## Examples

This replaces all items in chests within a 100 blocks with the "simple_dungeon" loottable:
```
/reloot generateNewLoot minecraft:chests/simple_dungeon 100 replace
```

This adds items from the "desert_pyramid" loottable into every chest in all loaded chunks:
```
/reloot regenerateAllLoot minecraft:chests/desert_pyramid keep
```

This adds items from the "random_rarity_chest" loottable into every chest and refills them after 60 seconds.
```
/reloot regenerateAllLoot reloot:chests/random_rarity_chest replace refill 60
```


## What is a loottable?
A (chest) loottable is a .json file which tells minecraft what items to put in the chests. Minecraft comes with some by default, but many mods also add their own. See loottables already in Minecraft below.

Example of a "woodland mansion" loottable:

```
minecraft:chests/woodland_mansion
```

## ReLoot loottables
**To use with the mod, just write `reloot:chests/<loottable here>`**

- random_rarity_chest (this one picks randomly from the loottables below)
- common_chest
- rare_chest
- epic_chest
- legendary_chest

## Default minecraft chest loottables
**To use with the mod, just write `minecraft:chests/<loottable here>`**

- abandoned_mineshaft
- ancient_city
- ancient_city_ice_box
- bastion_bridge
- bastion_hoglin_stable
- bastion_other
- bastion_treasure
- buried_treasure
- desert_pyramid
- end_city_treasure
- igloo_chest
- jungle_temple
- jungle_temple_dispenser
- nether_bridge
- pillager_outpost
- ruined_portal
- shipwreck_map
- shipwreck_supply
- shipwreck_treasure
- simple_dungeon
- spawn_bonus_chest
- stronghold_corridor
- stronghold_crossing
- stronghold_library
- underwater_ruin_big
- underwater_ruin_small
- woodland_mansion


## Plans for the future (concepts)
- Other containers support including Trapped Chests, Barrels, etc.
- Multiple loottables support (for example: 30% simple_dungeon and 70% desert_pyramid).
- Modded containers such as safes, drawers which can replace chests with a command.
- Built-in loottables that support different mods (example: reloot:chests/rare_chest which contains mostly rare items or other themed loottables).
- GUI? (less likely)