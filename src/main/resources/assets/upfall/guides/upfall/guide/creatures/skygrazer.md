---
navigation:
  title: Skygrazer
  icon: upfall:skygrazer_spawn_egg
  parent: creatures/index.md
  position: 40
item_ids:
  - upfall:skygrazer_spawn_egg
  - upfall:raw_skygrazer_meat
  - upfall:cooked_skygrazer_meat
  - upfall:down_tuft
---

# Skygrazer

<GameScene zoom="3" interactive={true}>
  <Entity id="upfall:skygrazer" />
</GameScene>

The Skygrazer is the livestock of the Upfall: a fluffy grazer that provides food and soft down.

## Where it spawns

- Only in the Upfall, on Skygrass Block.
- Found in the Drifting Archipelago (most common) and Hanging Roots, in herds of 2 to 4. These biomes also place some animals when the terrain is generated.

## Behaviour

- Wanders islands, preferring grass to bare ground.
- Runs from [Cragmaw](cragmaw.md) within 12 blocks; Cragmaws in turn hunt it.
- Adults hit back when hurt; babies panic and flee instead.

## Stats

| Stat | Value |
|---|---|
| Health | 22 (11 hearts) |
| Melee damage | 4 (strong knockback) |
| Experience | 2 |

## Breeding

Feed two adults <ItemLink id="cloudberry" /> or <ItemLink id="gildfruit" /> to enter love mode. They also follow you while you hold either. A baby appears from the pair.

## Drops

- <ItemLink id="raw_skygrazer_meat" />: 1 to 3 (already cooked if the animal dies on fire).
- <ItemLink id="down_tuft" />: 1 to 3.
- Looting adds up to 1 extra of each per level.

### Cooked meat

<RecipeFor id="cooked_skygrazer_meat" />

Cook it in a furnace, smoker or campfire. Raw meat restores 3 hunger; cooked restores 8 with high saturation.

### Down Tuft

Four tufts craft into white wool, so a herd is a renewable wool source.

<Recipe id="upfall:white_wool_from_down_tuft" />

## Tips

- Build a fenced pasture on Skygrass; keep it away from Cragmaw areas and open edges.
- Cloudberries are the cheapest breeding food.
