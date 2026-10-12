---
navigation:
  title: Hastening Gear
  icon: upfall:hastening_gear
  parent: machines/index.md
  position: 20
item_ids:
  - upfall:hastening_gear
---

# Hastening Gear

<BlockImage id="hastening_gear" scale="4" float="right" />

The **Hastening Gear** speeds up whatever it faces: a furnace, or a patch of crops. It burns Kairite while it works.

## Crafting

Smith a Block of Redstone, a <ItemLink id="kairite_gear" /> and a [Hastening Gear Blueprint](index.md).

<RecipeFor id="hastening_gear" />

## Placing it

The gear is a flat plate that sticks to a block face, and it **faces the direction you were looking** when you placed it. Whatever is in front of it is what it accelerates.

<GameScene zoom="3" interactive={true}>
  <IsometricCamera yaw="225" pitch="30" />
  <Block id="minecraft:furnace" x="0" y="0" z="0" />
  <Block id="upfall:hastening_gear" x="1" y="0" z="0" p:facing="west" />
</GameScene>

### Furnaces

If the block in front is a furnace, blast furnace or smoker that is currently cooking, the gear gives it an extra tick every two ticks, so it works about **one and a half times faster**. Fuel is spent only while something is cooking.

### Crops

Otherwise the gear boosts growing plants in a 3x3x3 area centered two blocks in front of it. Crops, stems, saplings, cocoa, sweet berries and nether wart are affected, and their growth is roughly **doubled**. Fuel is spent only while something in the area is still growing.

## Fuel

Right-click the gear with <ItemLink id="kairite_dust" /> or a <ItemLink id="kairite_gear" />. Right-click with an empty hand to see the fuel percentage. A full tank is about 20 minutes of active work; a dust is worth about two minutes and a gear about eighteen.

A hopper or other automation can insert Kairite too. A comparator reads the fuel level. If you break the gear, the unspent fuel comes back as Kairite Dust.
