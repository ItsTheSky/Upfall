---
navigation:
  title: Stalled Engine
  icon: upfall:heartgear
  parent: structures/index.md
  position: 70
item_ids:
  - upfall:drain_valve
  - upfall:engraved_rustslate
  - upfall:heartgear
---

# Stalled Engine

<BlockImage id="heartgear" scale="4" float="right" />

The Stalled Engine is the ruined **pump hall** of the Rustworks: a long industrial hall, half collapsed, built around a
huge broken cog wheel, the **Heartgear**. A sealed vault at the back holds the best loot of the biome. Opening it is a
small puzzle: you must turn three **Drain Valves** to empty it.

Finding the engine unlocks the advancement *Stalled, Not Dead*.

<br clear="all" />

## Finding it

- Only in the **Rustworks** biome, on flat enough ground, away from the cloud edge.
- Rare: roughly one per region of 12 by 12 chunks.
- Two big pipes leave the hall toward the nearest edge of the island and plunge into the cloud sea.
- Another pipe, the **Spire Main**, runs straight out of the hall in the direction of the
  [Chronospire](chronospire.md) and ends, broken and leaking, about 31 blocks out. Follow it back to find the hall, or
  follow it forward to find the Chronospire.

## Layout

The hall is about 25 blocks long and 19 deep. One corner has caved in; the rest still has walls, barred windows and a
roof of beams. Its position and rotation vary, so the directions below are relative to the hall.

- **Center:** the Heartgear machine. A round wheel of cog plating stands upright on a low platform. Spokes of
  <ItemLink id="rusty_pipe" /> (one of them leaking) join it to the <ItemLink id="heartgear" /> at the hub. A lectern
  with a book stands in front of it.
- **Gantries:** iron grating catwalks run along two walls, reached by ladders.
- **Foreman's office:** a walled room in one back corner, with a lectern and a Sky Cache.
- **Two tanks:** at the front of the hall, outside the wall, stand two big tanks. One is intact; the other is a ruin with a
  Sky Cache inside.
- **The vault:** at the back, behind a 3 by 3 opening filled with **Stagnant Mist** and framed with
  <ItemLink id="engraved_rustslate" />. The Drain Regulator is mounted on the wall right next to this opening.

## Solving the vault puzzle

The vault is filled from floor to ceiling with **Stagnant Mist**, a grey mist that is as solid as stone and cannot be
broken. The **Drain Regulator** beside the door is just as unbreakable until the puzzle is solved.

1. **Look at the regulator.** Right-click it with an empty hand: the action bar shows `Drain valves open: 0 / 3`. This is
   your progress counter.
2. **Find the three Drain Valves** and right-click each one to open it. Click again to close it. They are identified
   by the pipe handle on a plate on the wall:
   - **The foreman's office.** A walled room in a back corner of the hall, entered through a one-block doorway. The valve is
     on the far wall of the room, about two blocks above the floor, with a dark Engraved Rustslate block close by. The foreman's
     log lies on a lectern in the same room.
   - **The north gantry.** Climb the ladder at the end of the grating catwalk that runs along the wall opposite the vault.
     The valve is set in that wall, about two blocks above the catwalk.
   - **The intact tank.** One of the two tanks outside the wall opposite the vault is still whole. A gap in the hall wall
     leads into it. The valve is low on the inside, next to the central pipe.
3. **Keep all three open.** The regulator checks them every half second. Each new valve makes it clank. When the counter
   reaches 3 / 3 it starts to drain by itself: the mist disappears **layer by layer from the top down**, in about
   two seconds, and the regulator turns to a drained state.
4. Walk into the vault. Right-click the regulator again for the message *The vault has been drained*. From then on it can be
   broken like any other block (it drops nothing).

If a valve is closed before the third one is opened, nothing is lost: just reopen it. Once all three are open, the drain
cannot be stopped.

You can craft your own <ItemLink id="drain_valve" /> from a <ItemLink id="pipe_valve" /> and <ItemLink id="kairite_dust" />. It turns
on and off anywhere, but only a valve of the engine is wired to the regulator.

<RecipeFor id="drain_valve" />

## Loot

### The vault chest

A Sky Cache at the far end of the vault:

- **1 to 2 blueprints:** Phase Watch (25%), Gearlift (20%), Pneumatic Pipe (20%), Hastening Gear (15%), Void Tether
  (12.5%) or Rewinder (7.5%).
- **2 to 3 rolls** of Kairite Gear (1 to 3) or Kairite Dust (3 to 8).
- **3 to 5 rolls** among Iron Ingot, Copper Ingot, Redstone, Chain, Gold Ingot, Nimbusite Ingot, Clock, Compass and Diamond.

The blueprints are smithing templates; take them to the machines chapter to learn what they do:
see [Machines & Quantum](../machines/index.md).

### Hall caches

Three more Sky Caches are hidden in the hall: at the top of the ladder at the end of a gantry, in the foreman's office, and in
the ruined tank.

- **About 46% for a blueprint** (the vault list, plus the Veil Mask Blueprint, which is the most common).
- Kairite Dust (2 to 6) and, rarely, a Kairite Gear.
- 2 to 5 rolls of Iron Nuggets, Iron, Copper, Chain, Redstone, Cobbled Rustslate, Rusty Cog, Rusty Pipe or Lantern.
- 12% chance of a Shifting Vault Map.

### The Heartgear

The <ItemLink id="heartgear" /> itself can be taken as a trophy. It is very hard and needs a **diamond pickaxe**. It has a
dim glow and is not used in any recipe.

## Building blocks

<ItemLink id="engraved_rustslate" /> is the carved, dark Rustslate used for the engine's trim. You can make it at a stonecutter:

<RecipesFor id="engraved_rustslate" />

## Stagnant Mist

**Stagnant Mist** only exists in the vault and cannot be broken by any tool. It also does not hurt you; it
simply blocks the way until the Drain Regulator removes it.

## Lore books

Three books are on lecterns. The first one is a good reminder of the puzzle.

### Pump Hall Log (by Foreman Ives, in the office)

> Day 212. Pressure holds at the third gauge. The Horologists came again with their brass charts and asked for more flow toward the Spire.
>
> They say their tower turns with the days of the world below, and that it must drink to keep its hours. We send it what the clouds give us.
>
> Day 240. The Heartgear stutters. The vault is flooded with stagnant mist and sealed until the clockmakers answer.
>
> Should anyone need the vault: three drain valves feed the regulator. One in the west tank, one on the north gantry, one here in my office. Open all three.

### Notes on the Spire Main (by the Third Horologist, near the Heartgear)

> The Spire Main leaves this hall and runs straight to the Chronospire. We laid it true: even broken, the pipe still points home.
>
> Every phase of the Spire was tuned against this engine. Dawn, Noon, Dusk, Midnight: four strokes of a single heart.
>
> Should the Heartgear stop, the Spire will keep turning on its own memory. But it will forget the hands that wound it.

### To the One Who Drains (by the Horologists, in the vault)

> If you read this, the mist is gone and the regulator still obeys. Good. The plans in this vault are ours.
>
> Kairite turns their gears. Mine it when the vein pulses brightest, or you will only gather dust.
>
> Carry the plans to a smithing table. And if you climb the Chronospire, tell it the engine is stalled, not dead.
