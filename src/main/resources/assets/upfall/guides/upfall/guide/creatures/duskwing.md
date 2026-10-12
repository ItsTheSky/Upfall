---
navigation:
  title: Duskwing
  icon: upfall:duskwing_spawn_egg
  parent: creatures/index.md
  position: 20
item_ids:
  - upfall:duskwing_spawn_egg
  - upfall:duskwing_plume
---

# Duskwing

<GameScene zoom="2" interactive={true}>
  <Entity id="upfall:duskwing" />
</GameScene>

The Duskwing is a hostile gliding raptor that patrols the open sky and dives on anyone it spots.

## Where it spawns

- Only in the Upfall, in open air (it needs a free block above and below) between Y 70 and 240.
- Found in every biome; slightly more often in Gilded Ridge, less in the Mistsea.
- It never exists on Peaceful difficulty: any Duskwing is removed there.

## Behaviour

It tracks players within 40 blocks and repeats a three-step pattern:

1. **Circle:** it orbits around you about 7 blocks above, for roughly 2.5 to 5 seconds.
2. **Dive:** once it is above you with a clear line of sight, it plunges straight at you.
3. **Climb:** after a hit it swoops away and climbs before circling again.

If a dive misses, it gives up after about two seconds and goes back to circling.

## Stats

| Stat | Value |
|---|---|
| Health | 26 (13 hearts) |
| Armor | 4 |
| Melee damage | 6.75 |
| Attack knockback | 0.8 |
| Experience | 10 |

## Drops

- <ItemLink id="duskwing_plume" />: 1 to 2.
- Feather: 0 to 2.
- Looting adds up to 1 extra of each per level.

The plume works as fletching in a custom arrow recipe: a flint, a stick and a plume make 6 arrows.

<Recipe id="upfall:arrow_from_duskwing_plume" />

## Tips

- Shoot it during the circle phase; the dive is fast and hard to dodge.
- Open sky is its territory. Fighting from under cover limits where it can dive from.
- Watch your footing on small islands while you dodge: a fall is as dangerous as the bird.
