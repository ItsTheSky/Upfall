---
navigation:
  title: Rustslate & Plating
  icon: upfall:rusty_plating
  parent: blocks/index.md
  position: 30
item_ids:
  - upfall:rustslate
  - upfall:rustslate_stairs
  - upfall:rustslate_slab
  - upfall:rustslate_wall
  - upfall:cobbled_rustslate
  - upfall:cobbled_rustslate_stairs
  - upfall:cobbled_rustslate_slab
  - upfall:cobbled_rustslate_wall
  - upfall:polished_rustslate
  - upfall:polished_rustslate_stairs
  - upfall:polished_rustslate_slab
  - upfall:polished_rustslate_wall
  - upfall:rustslate_bricks
  - upfall:rustslate_brick_stairs
  - upfall:rustslate_brick_slab
  - upfall:rustslate_brick_wall
  - upfall:rusty_plating
  - upfall:cog_plating
  - upfall:rusty_cog
  - upfall:rusty_pipe
---

# Rustslate & Plating

The Rustworks is the rusted, machine-choked corner of the Upfall. Its stone is **Rustslate**, a near-black slate; its walls, gears and pipes are made of rust-colored metal plating.

<Row gap="10">
  <BlockImage id="rustslate" scale="3" />
  <BlockImage id="cobbled_rustslate" scale="3" />
  <BlockImage id="polished_rustslate" scale="3" />
  <BlockImage id="rustslate_bricks" scale="3" />
  <BlockImage id="rusty_plating" scale="3" />
  <BlockImage id="cog_plating" scale="3" />
  <BlockImage id="rusty_cog" scale="3" />
</Row>

## Rustslate

- It is the rock of the Rustworks biome. Under the top layers of Oxide Turf and Rustsoil (see [Soils & Plants](soils_and_plants.md)), everything is Rustslate.
- It needs a pickaxe. Mining it drops **Cobbled Rustslate**; use Silk Touch to keep plain Rustslate. Kairite ore is buried in it.
- Cobbled and Polished Rustslate are also used by ruins in the region.

Crafting follows the same pattern as the other stone families:

- **Rustslate**: smelt Cobbled Rustslate.
- **Polished Rustslate**: 2x2 of Rustslate, gives 4.
- **Rustslate Bricks**: 2x2 of Polished Rustslate, gives 4.
- Stairs, slabs and walls for all four variants. In a Stonecutter one block gives 1 stair, 1 wall or 2 slabs, and Rustslate can be cut directly into polished and brick forms.

<RecipeFor id="polished_rustslate" />
<RecipeFor id="rustslate_bricks" />
<RecipeFor id="rustslate_brick_stairs" />

<ItemGrid>
  <ItemIcon id="rustslate" />
  <ItemIcon id="rustslate_stairs" />
  <ItemIcon id="rustslate_slab" />
  <ItemIcon id="rustslate_wall" />
  <ItemIcon id="cobbled_rustslate" />
  <ItemIcon id="cobbled_rustslate_stairs" />
  <ItemIcon id="cobbled_rustslate_slab" />
  <ItemIcon id="cobbled_rustslate_wall" />
  <ItemIcon id="polished_rustslate" />
  <ItemIcon id="polished_rustslate_stairs" />
  <ItemIcon id="polished_rustslate_slab" />
  <ItemIcon id="polished_rustslate_wall" />
  <ItemIcon id="rustslate_bricks" />
  <ItemIcon id="rustslate_brick_stairs" />
  <ItemIcon id="rustslate_brick_slab" />
  <ItemIcon id="rustslate_brick_wall" />
  <ItemIcon id="rusty_plating" />
  <ItemIcon id="cog_plating" />
  <ItemIcon id="rusty_cog" />
  <ItemIcon id="rusty_pipe" />
</ItemGrid>

## Plating and pipes

Everything metallic starts from the **Rusty Plate**: combine a Copper Ingot with <ItemLink id="kairite_dust" /> to get 4 plates.

<RecipeFor id="rusty_plate" />

| Block | Recipe | Notes |
|-------|--------|-------|
| <ItemLink id="rusty_plating" /> | 2x2 Rusty Plates, gives 4 | Plain metal wall. |
| <ItemLink id="cog_plating" /> | Rusty Plates and Rusty Plating in a checker pattern, gives 4 | Plating with a gear motif. |
| <ItemLink id="rusty_cog" /> | A column of Rusty Plates with a Rusty Plating in the middle row | Large decorative gear; it rotates to the axis you place it on. |
| <ItemLink id="rusty_pipe" /> | 2x3 Rusty Plates, gives 6 | Thin pipe that rotates to the axis you place it on. |

<Row gap="10">
  <BlockImage id="rusty_plating" scale="4" />
  <BlockImage id="cog_plating" scale="4" />
  <BlockImage id="rusty_cog" scale="4" />
  <BlockImage id="rusty_pipe" scale="4" />
</Row>

<RecipeFor id="rusty_plating" />
<RecipeFor id="cog_plating" />
<RecipeFor id="rusty_cog" />
<RecipeFor id="rusty_pipe" />

These metal blocks need a pickaxe and are tougher than Rustslate. In the wild you will also meet <ItemLink id="leaking_pipe" /> and <ItemLink id="exhaust_vent" />, which are part of the same rusty scenery.
