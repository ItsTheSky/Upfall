---
navigation:
  title: Quantum Pedestal
  icon: upfall:quantum_pedestal
  parent: machines/index.md
  position: 90
item_ids:
  - upfall:quantum_pedestal
---

# Quantum Pedestal

<BlockImage id="quantum_pedestal" scale="4" float="right" />

The **Quantum Pedestal** enchants Levitite gear with permanent **upgrades** through an **incantation**. Quantum things exist only while observed, and so does the ritual: if you look away, it collapses.

## Crafting

Smith a <ItemLink id="pedestal" />, a <ItemLink id="quantum_stone" /> and a [Quantum Pedestal Blueprint](index.md). The blueprint is found in the Shifting Vault.

<RecipeFor id="quantum_pedestal" />

The Quantum Stone comes from pearlstone and <ItemLink id="flicker_shard" />:

<RecipeFor id="quantum_stone" />

## Setting up the ring

An incantation needs the Quantum Pedestal in the center and **eight ordinary Pedestals** around it, all at the same height, **3 blocks** away from the center:

- four **side** Pedestals, straight north, south, east and west;
- four **corner** Pedestals, on the diagonals.

<GameScene zoom="1" interactive={true}>
  <IsometricCamera yaw="225" pitch="45" />
  <Block id="upfall:quantum_pedestal" x="0" y="0" z="0" />
  <Block id="upfall:pedestal" x="3" y="0" z="0" />
  <Block id="upfall:pedestal" x="-3" y="0" z="0" />
  <Block id="upfall:pedestal" x="0" y="0" z="3" />
  <Block id="upfall:pedestal" x="0" y="0" z="-3" />
  <Block id="upfall:pedestal" x="3" y="0" z="3" />
  <Block id="upfall:pedestal" x="-3" y="0" z="3" />
  <Block id="upfall:pedestal" x="3" y="0" z="-3" />
  <Block id="upfall:pedestal" x="-3" y="0" z="-3" />
</GameScene>

Each Pedestal holds one item: right-click it with the item to set it down, and right-click with an empty hand to take it back.

| Pedestal | Item |
|---|---|
| Center (Quantum Pedestal) | the Levitite piece to upgrade |
| 4 corners | a <ItemLink id="quantum_stone" /> each |
| 4 sides | the four offerings of the upgrade, in any order |

## Performing the incantation

1. **Sneak** and right-click the Quantum Pedestal with an **empty hand**.
2. Keep **watching it** for 5 seconds. You must face it, have a clear line of sight, be within 64 blocks and have it lit (light level 4 or more at the pedestal, or carry a light source such as a torch).
3. Sparks flow from the ring to the center. On success the item on the Quantum Pedestal now carries the upgrade, and the **eight ring items are consumed**.

If you look away, die or move out of sight, the incantation **collapses and the ring items are still consumed**. Keep your eyes on it.

The game tells you why a start was refused: no item on the center, ring incomplete, offerings that match no incantation, the piece cannot receive that upgrade, the upgrade is already on it, or the piece is full.

### Limits

- An item can carry **2 upgrades** at most, each only once.
- An upgrade only works while the item is worn (armor) or held in the main hand (pickaxe, sword).
- Upgrades are shown in the item tooltip.

## Upgrades

### Helmet

#### Veilsight

Applies to: <ItemLink id="levitite_helmet" />. Filters the Veil Gas so it no longer harms you, and reveals it, like the <ItemLink id="veil_mask" />.

Offerings: <ItemLink id="veil_mask" />, 3 <ItemLink id="mistglow_dust" />.

### Chestplate

#### Uncertainty

Applies to: <ItemLink id="levitite_chestplate" />. Each hit from an attacker has a **15% chance** to be dodged entirely.

Offerings: 2 <ItemLink id="bottled_veil_gas" />, 2 <ItemLink id="mist_gel" />.

#### Zipline

Applies to: <ItemLink id="levitite_chestplate" />. With an **empty hand**, right-click a rope or a <ItemLink id="zipline_anchor" /> to latch onto the line and ride it.

Offerings: 2 <ItemLink id="rope_spool" />, <ItemLink id="zipline_anchor" />, <ItemLink id="duskwing_plume" />.

### Leggings

#### Anchored

Applies to: <ItemLink id="levitite_leggings" />. You are immune to knockback.

Offerings: 2 <ItemLink id="kairite_gear" />, 2 <ItemLink id="duskslate" />.

#### Solidifier

Applies to: <ItemLink id="levitite_leggings" />. Mist becomes solid under your feet: you **bounce** on it (about 85% of your landing speed, at least a 2.5 block hop) without taking fall damage. **Sneak** to sink into the mist as usual.

Offerings: 2 <ItemLink id="dense_cloud" />, 2 <ItemLink id="mist_gel" />.

### Boots

#### Mosswalker

Applies to: <ItemLink id="levitite_boots" />. Run much faster on moss: your movement speed gains +0.08 while standing on moss (the base walking speed is 0.1).

Offerings: 2 <ItemLink id="verdigris_moss" />, <ItemLink id="minecraft:moss_block" />, <ItemLink id="lumen_moss" />.

### Any Levitite armor piece

#### Rewind

Applies to: any piece of Levitite armor. When you would die, you are instead brought back to the last solid ground you stood on a few seconds earlier, with **30% of your maximum health** and a moment of invulnerability. It works **once per day** (one full Minecraft day of 20 minutes) and requires that you had stood on solid ground recently, like the <ItemLink id="rewinding_hourglass" />.

Offerings: <ItemLink id="rewinding_hourglass" />, 3 <ItemLink id="chrono_gnomon" />.

### Pickaxe

#### Superposition

Applies to: <ItemLink id="levitite_pickaxe" />. When you mine an ore, **one identical ore touching it** is mined as well, by the same pickaxe and with its own drops.

Offerings: <ItemLink id="raw_nimbusite" />, <ItemLink id="levitite_shard" />, <ItemLink id="kairite_gear" />, <ItemLink id="cloud_pearl" />.

### Sword

#### Entanglement

Applies to: <ItemLink id="levitite_sword" />. Hit one creature, then a second within 10 seconds: the two are **entangled** for 10 seconds. Any damage dealt to one is mirrored to the other for **50%** of its amount.

Offerings: 2 <ItemLink id="cragmaw_fang" />, 2 <ItemLink id="flicker_shard" />.
