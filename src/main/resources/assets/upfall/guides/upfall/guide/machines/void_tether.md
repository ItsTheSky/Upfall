---
navigation:
  title: Void Tether
  icon: upfall:void_tether
  parent: machines/index.md
  position: 50
item_ids:
  - upfall:void_tether
---

# Void Tether

<BlockImage id="void_tether" scale="4" float="right" />

Dropped something off the edge of the world? The **Void Tether** catches items that are falling into the void and brings them back.

## Crafting

Smith an Ender Pearl, a <ItemLink id="kairite_gear" /> and a [Void Tether Blueprint](index.md).

<RecipeFor id="void_tether" />

## Using it

Place it **above** the place where items fall. Twice a second it scans an area reaching 16 blocks in every horizontal direction, from its own height down to below the bottom of the world.

An item counts as lost when it is plunging with nothing solid or liquid under it all the way down, or when it is close to the bottom of the world. Up to 16 items are caught per scan.

A caught item is sent into any inventory touching the Tether (chest, barrel, hopper and so on, on any side). If there is none, or it is full, the item pops out on top of the Tether with a short pickup delay.

No fuel is needed.
