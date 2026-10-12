---
navigation:
  title: Shifting Vault
  icon: upfall:shifting_chest
  parent: structures/index.md
  position: 80
item_ids:
  - upfall:vault_bricks
  - upfall:vault_glass
  - upfall:vault_lamp
  - upfall:vault_tiles
  - upfall:shifting_chest
  - upfall:crumbling_floor
  - upfall:frost_glass
  - upfall:pedestal
  - upfall:scattered_bones
  - upfall:abandoned_pickaxe
  - upfall:spent_lantern
  - upfall:cracked_veil_mask
---

# Shifting Vault

<BlockImage id="shifting_chest" scale="4" float="right" />

The Shifting Vault is a **glass maze buried deep inside an island**. At the end of it waits a **Shifting Chest** that holds the
Quantum Pedestal Blueprint, but the chest is a quantum object: **it only stays where it is while somebody is watching it**.
The whole vault is built to make you look away.

The vault is the gate to the quantum machines. See [Machines & Quantum](../machines/index.md) for what you can do with its loot.

<br clear="all" />

## Finding it

- Underground, in the **Drifting Archipelago, Hanging Roots, Gilded Ridge and Rustworks** biomes, inside islands thick
  enough to hide it (it needs solid rock all around).
- Very rare: by default about one per region of 56 by 56 chunks (the server owner can change this).
- There is nothing to see on the surface. The only way in is a **long winding tunnel** (60 to 120 blocks) bored through the
  island, which is **filled with Veil Gas**.
- The vault is about 24 by 29 blocks and 10 high, with an antechamber just after the tunnel.

> **Bring a Veil Mask.** Veil Gas is invisible and hurts anyone whose head is in it (one heart per second). A
> <ItemLink id="veil_mask" /> filters it and makes it visible. The mask is crafted at a smithing table from a
> <ItemLink id="veil_mask_blueprint" />, an iron helmet and a <ItemLink id="kairite_gear" />; the blueprint is found in
> the [Stalled Engine](stalled_engine.md).

## Layout

Everything in the vault is **unbreakable**: <ItemLink id="vault_bricks" />, <ItemLink id="vault_tiles" />,
<ItemLink id="vault_glass" /> and the <ItemLink id="vault_lamp" /> cannot be mined.

- The floor of <ItemLink id="vault_tiles" /> sits above a layer of **Veil Gas**. The walkable level is a **7 by 7 maze** of
  small cells (2 by 2 blocks each), separated by walls of Vault Glass, so you can see through everything.
- The maze entrance is in the middle of the near side, straight from the antechamber. A ladder in the antechamber climbs
  out of the gas layer under the floor.
- There are **three possible chest spots**: the far end of the maze (where the chest starts), and the middle of each side wall.
- The maze has loops, so you can get to a spot by several paths.

<GameScene zoom="2" interactive={true}>
  <Block id="upfall:vault_tiles" x="0" y="0" z="0" />
  <Block id="upfall:vault_tiles" x="1" y="0" z="0" />
  <Block id="upfall:vault_glass" x="0" y="1" z="0" />
  <Block id="upfall:frost_glass" x="1" y="1" z="0" />
  <Block id="upfall:shifting_chest" x="1" y="1" z="1" p:facing="north" />
  <Block id="upfall:vault_tiles" x="1" y="0" z="1" />
  <Block id="upfall:vault_lamp" x="1" y="3" z="1" />
  <IsometricCamera yaw="225" pitch="30" />
</GameScene>

## How the chest shifts

The chest "observes" the player. It is **observed** when **all** of these are true for any player:

- The chest is within **64 blocks**.
- The player **looks toward it** (the chest must be within about 50 degrees of the center of your view).
- There is a **clear line of sight** (glass is clear; opaque blocks, closed doors and frosted glass block it).
- The chest is **lit** (light level 4 or more) **or** the player is holding a light source.

If a player is near but the chest is **not observed** and nobody has its screen open, it **teleports** to another free
chest spot that nobody is watching either, with a puff of sparks and a sound. It then waits at least **1.5 seconds** before it can jump again, and it
carries its contents with it.

Open the chest and it stays put for as long as you look at the contents. **The chest cannot move while it is open.**

### How to catch it

1. Walk toward the chest **without looking away**. Strafe, do not turn your head.
2. If you lose it, find where it went: a spark marks the spot, and the chest is always at one of the three spots.
3. Cut through the maze toward the closest spot, keeping the chest in view when you can.
4. Once you are close, open it. The chest holds its position while open.

A good trick: hold a light source (a torch is enough) in either hand. With a light in your hand, the chest counts as lit from
up to 64 blocks away, which defeats the lights-out trap below.

## The tricks of the vault

| Trap | What happens |
| --- | --- |
| **Frost Glass** | About one maze wall in five is <ItemLink id="frost_glass" />. When you get within about two blocks, it frosts over for 5 seconds and blocks your line of sight, which gives the chest time to jump. |
| **Crumbling Floor** | Up to five patches of the floor are <ItemLink id="crumbling_floor" />. Step on one and a short moment later it and its neighbors collapse, dropping you into the **Veil Gas layer** below. The floor grows back after 10 seconds, once nobody is standing in the gap. |
| **Lights out** | Two cells of the maze are dark zones. Entering one switches **every <ItemLink id="vault_lamp" /> off** for 7 seconds. Without a light of your own, the chest is no longer lit, so it is no longer observed. |
| **Whispers** | While you are inside the vault, you sometimes hear whispers **behind you**. They are just a lure to make you turn around. |

Falling into the gas layer is the real danger of the vault: your head is in the gas and you take damage until you walk
under the floor back to the ladder in the antechamber. A Veil Mask removes the damage.

## Loot

The Shifting Chest (a 27-slot chest) contains:

- **Always:** one <ItemLink id="quantum_pedestal_blueprint" />.
- **Always:** 2 to 4 <ItemLink id="quantum_stone" />.
- **2 to 4 rolls** among: Flicker Shard (3 to 8), Quantum Stone (1 to 2), Bottled Veil Gas (1 to 3), Diamond (1 to 3), Ender
  Pearl (1 to 3) and Amethyst Shard (3 to 8).

Opening the chest unlocks an advancement.

## Pedestals

The <ItemLink id="pedestal" /> is a decoration block that holds a single item: right-click with an item to place it and right-click with
an empty hand to take it back. Eight of them around a Quantum Pedestal are the ring of an incantation (see the
Quantum chapter). Craft them from pearlstone:

<RecipeFor id="pedestal" />

## Dressing blocks

Four props found in gas caves (and not in the vault itself) tell the story of the explorers who never came back. They are all
decorative, and break instantly when you hit them.

<Row gap="10">
  <BlockImage id="scattered_bones" scale="3" />
  <BlockImage id="abandoned_pickaxe" scale="3" />
  <BlockImage id="cracked_veil_mask" scale="3" />
  <BlockImage id="spent_lantern" scale="3" />
</Row>

- <ItemLink id="scattered_bones" /> and <ItemLink id="cracked_veil_mask" /> are only found as scenery.
- <ItemLink id="abandoned_pickaxe" /> and <ItemLink id="spent_lantern" /> can be crafted from Rusty Plates:

<Row gap="10">
  <RecipeFor id="abandoned_pickaxe" />
  <RecipeFor id="spent_lantern" />
</Row>
