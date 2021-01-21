# CivOres
Custom, player and biome-specific ore generation designed for Civilisation-style Minecraft servers.

### Index
- [The problem](#the-problem)
    - [Biome-specific ore generation](#biome-specific-ore-generation)
    - [Player-specific ore generation](#player-specific-ore-generation)
- [The solution](#the-solution)
    - [Custom ore generation](#custom-ore-generation)
    - [Packet manipulated ores](#packet-manipulated-ores)

## The problem
### Biome-specific ore generation
Typically, Civilisation-style Minecraft servers have sparse maps divided into biomes which have natural differences.
One of these differences may be certain ores being more or less plentiful in certain biomes than others.

### Player-specific ore generation
As Civilization-style Minecraft servers generally have a finite world, it may pose an issue for newer players in the server later in a wipe.
It may be ideal if each player had their own set of ores only visible (and mineable) by them.
Although this would not reward exploration or punish players for mining in already harvested land,
so it may be beneficial to reduce the amount of ores a player has in already mined land.

## The solution
### Custom ore generation
To allow for different quantities of ores in different biomes and for each individual player,
a custom ore generation engine would have to be developed.
This may change, though I currently plan to use 3D OpenSimplex2S noise maps for ore generation,
requiring different thresholds and frequencies for different ores, biomes, and Y levels, to be similar to vanilla.

### Packet manipulated ores
To allow for each player to see their own individual ores, manual packets will have to be sent to each player about their ores.
Then, once a player mines a block, it would check if that specific block correlated to an ore which they see,
and swiftly change it to that ore on the server before breaking it.

As these ores would be done entirely through packets, it seems natural to also incorporate an anti-xray on ores,
as this would probably not raise the complexity too much.
