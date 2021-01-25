# CivOres
Custom, player and biome-specific ore generation designed for Civilisation-style Minecraft servers.

### Index
- [The problem](#the-problem)
    - [Biome-specific ore generation](#biome-specific-ore-generation)
    - [Player-specific ore generation](#player-specific-ore-generation)
- [The solution](#the-solution)
    - [Custom ore generation](#custom-ore-generation)
    - [Packet manipulated ores](#packet-manipulated-ores)
- [Installation](#installation)
    - [Download](#download)
        - [Download ProtocolLib](#download-protocollib)
    - [Configuration file](#configuration-file)
- [Technical Overview](#technical-overview)
    - [OreChunk](#orechunk)
        - [Multi-threading](#multi-threading)
        - [Generation](#generation)
    - [Player metadata](#player-metadata)

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

## Installation
### Download
You may download the latest version of this plugin (which supports 1.16+), from [the latest release](../../releases/latest).

You may also see all previous releases on [the releases page](../../releases/).
If you require an older version, the release title should specify the version of the release.

#### Download ProtocolLib
Currently, the latest release requires ProtocolLib v4.6.0-SNAPSHOT, which can be downloaded from [ProtocolLib's Jenkins](https://ci.dmulloy2.net/job/ProtocolLib/).
Though, older versions may require different versions, and this will be specified in their release description.

### Configuration file
TODO: add configuration file instructions.

## Technical Overview
### OreChunk
An OreChunk will be the class which generates and caches all the ores in a chunk (16x256x16) for one specific player.
This will effectively contain a HashMap with the key being the co-ordinates of the ore, and the value being the material of the ore.
These OreChunks will generate the exact same way given the same input parameters (player, world, settings) every time using noise seeding.
This is as to produce consistent ores across the map, without storing it anywhere in a database.

#### Multi-threading
Given this stateless nature, it would allow for this to run on a separate thread, which would make the complex calculations (virtually) free.
This is because Minecraft servers are by nature single-threaded, and this one thread will almost always be the bottleneck.
Due to this, Minecraft servers typically have an abundance of unused cores and threads which are (virtually) free to utilise.

Though, of course, multi-threading would restrict our API access during generation.
Therefore, OreChunks will have to be generated,
then set a flag once generated telling the synchronous checker to run all necessary synchronous tasks.
For example, sending all the necessary packets to the player (which would require having access to the world) which would require synchronicity.

#### Generation
Generation will be done using 3D OpenSimplex2S noise using the [FastNoise library](https://github.com/Auburn/FastNoise) for Java.
Ores will generate when the noise value at a particular co-ordinate exceeds the threshold required for generating it.
This threshold would be generated via a combination of the Y level, versus it's optimal Y level, and the biome's spawnrates.

As for differentiating between biomes, a sample will be taken at the centre of the chunk.
The chunk will then be generated using that one biome for the entire chunk.

### Player metadata
In order to store these OreChunks, they will need to be stored in a HashMap on the player's metadata,
with the key being the chunk's key, and the value being the OreChunk.
As this will contain all the OreChunks, this would also ensure that new OreChunks are generated, and old are disposed when needed.
