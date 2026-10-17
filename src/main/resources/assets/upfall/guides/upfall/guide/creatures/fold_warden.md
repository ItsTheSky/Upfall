---
navigation:
  title: Fold Warden
  icon: upfall:fold_heart
  parent: creatures/index.md
  position: 70
item_ids:
  - upfall:fold_warden_spawn_egg
  - upfall:fold_heart
  - upfall:fold_lattice
---

# Fold Warden

<GameScene zoom="1" interactive={true}>
  <Entity id="upfall:fold_warden" />
</GameScene>

The Fold Warden is the guardian sealed in the heart of every [Tilted Reliquary](../structures/tilted_reliquary.md).
The last Surveyor who kept the vaults folded themself into the machine. It fights in two forms: a tilted lattice
cube, then a wyrm made of its own unfolded fragments.

## Waking it

- It appears when the **fourth Fold Socket** of the core cube is filled and the Fold Seals dissolve.
- It spends 5 seconds rising out of the seal chamber. It cannot be hurt while it wakes. Once the seals are gone, the chamber floor
  closes back into lattice stone.
- Each reliquary has only one Warden. It never despawns, and it stays within about 24 blocks of the core.

## Phase 1: the Fold

The Warden hovers above you as a cube tilted on its edge, with four **Fold Satellites** orbiting around it.

- **Shield:** while at least one satellite is alive, the core cannot be hurt. Destroy the satellites first
  (16 health each) to open its shutters and expose the eye. They reform after 30 seconds.
- **Satellite Slam:** a satellite marks a ring on the ground under a player, then crashes into it (8.8 damage, knockback).
- **Tilt:** the cube rolls 45 degrees and every player in the arena is pushed sideways for 4 seconds. Sparks on the
  ground show the direction beforehand. Sneaking halves the push.
- **Weight of Falling:** players are lifted for 2 seconds, then slammed down. Standing on a **filled Fold Socket**
  when it starts protects you.
- Touching the cube hurts (6.6 damage).

## Phase 2: the Plummet

At half health the cube **unfolds** into a wyrm of twelve lattice segments. It cannot be hurt while it unfolds.

- Its head takes full damage, and its body segments take half.
- **Coil:** it circles a player in a tightening ring. Its whole body hurts on contact.
- **Shed:** it hurls three tail segments at players. They fly back to it like boomerangs, so watch out on their return.
- **Updraft Dive:** it climbs high, then dives where you stood (11 damage). The impact leaves an updraft column for 4 seconds.
- **Lattice Breath:** its jaw glows for 1.5 seconds before it breathes a beam (6.6 damage, Slowness). The beam also locks
  its target in a cage of <ItemLink id="fold_lattice" />. The cage breaks easily and dissolves after about 8 seconds.
- Below a quarter of its health it becomes **enraged**: faster, shorter pauses, and it sheds five segments at a time.

## Stats

| Stat | Value |
|---|---|
| Health | 400 (200 per phase) |
| Armor | 10 |
| Knockback resistance | Full |
| Experience | 250 |

## Drops

- <ItemLink id="fold_anchor" />: always.
- <ItemLink id="fold_heart" />: always.
- <ItemLink id="fold_shard" />: 1 to 3.

## Tips

- Bring a bow for the satellites: they orbit out of melee reach most of the time.
- Remember where the four sockets are. They are your shelter from Weight of Falling.
- The deck has open edges: during a Tilt, sneak and move against the push.
- In phase 2, stay mobile. A pickaxe frees you from a lattice cage in a moment.
