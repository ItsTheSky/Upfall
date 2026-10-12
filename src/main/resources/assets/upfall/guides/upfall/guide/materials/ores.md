---
navigation:
  title: Ores
  icon: upfall:nimbusite_ore
  parent: materials/index.md
  position: 10
item_ids:
  - upfall:nimbusite_ore
  - upfall:duskslate_nimbusite_ore
  - upfall:levitite_ore
  - upfall:kairite_ore
  - upfall:pearled_cloud
  - upfall:flicker_shard_cluster
---

# Ores

Every ore of the Upfall, with where it generates and what you need to mine it. Heights are world Y levels in the Upfall dimension.

| Ore | Found in | Y range | Tool needed |
|---|---|---|---|
| Nimbusite Ore | Pearlstone, all biomes | 90 to 200 | Netherite tier or better |
| Duskslate Nimbusite Ore | Duskslate, all biomes | 90 to 200 | Netherite tier or better |
| Levitite Ore | Duskslate, Gilded Ridge only | 70 to 280 | Nimbusite pickaxe or better |
| Kairite Ore | Rustslate, Rustworks only | 74 to 240 | Iron pickaxe or better |
| Pearled Cloud | Dense Cloud, all biomes | 40 to 64 | Any tool |
| Flicker Shard Cluster | Gas cave walls | Underground | Any tool |

## Nimbusite

<Row gap="10">
  <BlockImage id="nimbusite_ore" scale="3" />
  <BlockImage id="duskslate_nimbusite_ore" scale="3" />
</Row>

<ItemLink id="nimbusite_ore" /> hides in pearlstone, and <ItemLink id="duskslate_nimbusite_ore" /> is the same ore embedded in duskslate. Veins are up to 7 blocks and common in every biome (Drifting Archipelago, Mistsea, Hanging Roots, Gilded Ridge, Rustworks).

Both are tough: they need a netherite pickaxe (or a Nimbusite/Levitite one) to drop anything, and give 3 to 7 experience. They drop <ItemLink id="raw_nimbusite" />, affected by Fortune; Silk Touch keeps the ore block.

Smelt the ore or the raw metal in a furnace or blast furnace:

<RecipeFor id="nimbusite_ingot" />

See [Nimbusite](nimbusite.md) for everything you can make with the ingots.

## Levitite

<BlockImage id="levitite_ore" scale="3" />

<ItemLink id="levitite_ore" /> only forms in the duskslate of the Gilded Ridge, in small veins of up to 4 blocks that are often half exposed. It is very hard (like obsidian and then some) and extremely blast resistant. Only Nimbusite or Levitite tools can mine it.

It drops <ItemLink id="levitite_shard" />. Smelting the ore itself also yields a shard:

<RecipeFor id="levitite_shard" />

Four shards plus four Nimbusite ingots make a [Levitite Ingot](levitite.md).

## Kairite

<BlockImage id="kairite_ore" scale="3" />

<ItemLink id="kairite_ore" /> is found in Rustslate in the Rustworks. It pulses on a 10 second cycle: dim, fading in, then a brief bright flash of about 2 seconds, then fading out again. The brighter it glows, the more light it gives off.

- Mined at any time, it drops 1 to 2 <ItemLink id="kairite_dust" /> (more with Fortune).
- Mined **at its brightest**, it drops a whole <ItemLink id="kairite_gear" /> instead.
- Silk Touch keeps the ore block.

Nearby ores pulse together in groups, so watch the glow of a vein and swing when it flares. Details in [Kairite](kairite.md).

## Pearled Cloud

<BlockImage id="pearled_cloud" scale="3" />

<ItemLink id="pearled_cloud" /> is a lump of cloud studded with pearls, buried in Dense Cloud at low altitude (Y 40 to 64). It breaks easily and drops 1 to 2 <ItemLink id="cloud_pearl" /> (Fortune increases it); Silk Touch keeps the block. Cloud Pearls are used for the [Nimbus Flask](gadgets.md).

## Flicker Shard Cluster

<BlockImage id="flicker_shard_cluster" scale="3" />

<ItemLink id="flicker_shard_cluster" /> crystals grow on the walls of gas caves and never sit still. Whenever nobody looks at one, it may jump to another wall nearby, and it always jumps once you start mining it while looking away. Keep your eyes on it while you mine, and it yields 1 to 3 <ItemLink id="flicker_shard" /> (Fortune helps); Silk Touch collects the whole cluster.

Read more in [Quantum materials](quantum_materials.md).
