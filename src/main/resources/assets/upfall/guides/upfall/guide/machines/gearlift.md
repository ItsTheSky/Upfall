---
navigation:
  title: Gearlift
  icon: upfall:gearlift
  parent: machines/index.md
  position: 10
item_ids:
  - upfall:gearlift
---

# Gearlift

<BlockImage id="gearlift" scale="4" float="right" />

The **Gearlift** is a short-range elevator. Stack Gearlifts in a vertical line and you can ride up and down between them instantly.

## Crafting

Smith a Piston, a <ItemLink id="kairite_gear" /> and a [Gearlift Blueprint](index.md) together. One operation gives two Gearlifts.

<RecipeFor id="gearlift" />

## Using it

- Stand on a Gearlift and **jump** to rise to the next Gearlift above.
- Stand on a Gearlift and **sneak** to descend to the one below.

The target must be within 64 blocks in the same column, and it needs two blocks of clear space above it. A Gearlift without room is skipped and the next one is tried. You arrive standing on top of the target and your fall distance is reset.

There is a short cooldown between rides. Spectators and riders of mounts cannot use it.

<GameScene zoom="2" interactive={true}>
  <IsometricCamera yaw="225" pitch="30" />
  <Block id="upfall:gearlift" x="0" y="0" z="0" />
  <Block id="upfall:gearlift" x="0" y="5" z="0" />
</GameScene>

## Tips

- Gearlifts need neither fuel nor redstone.
- Only the horizontal position matters: all Gearlifts must share the same X and Z.
