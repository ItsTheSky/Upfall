---
navigation:
  title: Mistjelly
  icon: upfall:mistjelly_spawn_egg
  parent: creatures/index.md
  position: 30
item_ids:
  - upfall:mistjelly_spawn_egg
  - upfall:mist_gel
  - upfall:cloudberry_jelly
---

# Mistjelly

<GameScene zoom="2" interactive={true}>
  <Entity id="upfall:mistjelly" />
</GameScene>

The Mistjelly is a glowing jellyfish that floats in the air above the mist layer, drifting slowly in groups.

## Where it spawns

- Only in the Upfall, at Y 36 to 72, in air or mist that has Mist or Dense Cloud within 24 blocks below it.
- Common in the Mistsea; also found in Drifting Archipelago, Hanging Roots and Rustworks. Not in Gilded Ridge.
- Spawns in groups of 1 to 3.

## Behaviour

- Peaceful as long as nobody hurts it, drifting slowly around its altitude band.
- When hit, it attacks the aggressor, and every Mistjelly within 12 blocks joins the fight.
- Each sting deals damage and applies Slowness II for 4 seconds; it can sting about once every 1.2 seconds.
- It loses interest after 30 seconds, or if you get more than 30 blocks away.

## Stats

| Stat | Value |
|---|---|
| Health | 16 (8 hearts) |
| Melee damage | 5 |
| Experience | 4 |

## Drops

- <ItemLink id="mist_gel" />: 1 to 3 (looting adds up to 1 per level).
- <ItemLink id="mistglow_crystal" />: 8% chance when killed by a player; +3% per looting level.

## Cloudberry Jelly

Mist Gel is the base of a food that grants the Weightless effect (see [The Upfall](../world/index.md)).

<RecipeFor id="cloudberry_jelly" />

Eating it restores 6 hunger and always gives Weightless for 12 seconds. It leaves the bowl behind and stacks to 16. Ingredients: 2 Mist Gel, 2 <ItemLink id="cloudberry" />, 1 Bowl.

## Tips

- Never poke a single jelly in a cluster: the whole group answers. Kill them one by one at range, or leave them alone.
- The slowness is dangerous over open sky; fight from a platform with a rim.
