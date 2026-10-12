---
navigation:
  title: Stillflow Cauldron
  icon: upfall:leaking_pipe
  parent: machines/index.md
  position: 80
item_ids:
  - upfall:pipe_valve
---

# Stillflow Cauldron

A **Stillflow Cauldron** is a Cauldron slowly filled with Stillflow, a glowing liquid that drips from the Rustworks' <ItemLink id="leaking_pipe" />. It turns <ItemLink id="kairite_dust" /> into <ItemLink id="kairite_gear" />, one for one, which is much cheaper than the nine dust of the crafting table.

## Making one

1. Craft a <ItemLink id="leaking_pipe" /> (a <ItemLink id="rusty_pipe" /> over a <ItemLink id="kairite_dust" />).
2. Place an empty Cauldron under it, up to 10 blocks below, with nothing solid in between.
3. Wait. From time to time a drop falls and the cauldron gains a level. After three drops it is full.

<GameScene zoom="3" interactive={true}>
  <IsometricCamera yaw="225" pitch="30" />
  <Block id="upfall:leaking_pipe" x="0" y="2" z="0" />
  <Block id="upfall:stillflow_cauldron" x="0" y="0" z="0" p:level="3" />
</GameScene>

## Using it

Right-click a **full** Stillflow Cauldron with a <ItemLink id="kairite_dust" />. The dust is consumed, you receive one <ItemLink id="kairite_gear" />, and the cauldron empties back into a normal Cauldron. Put a leaking pipe over it again to repeat.

Broken, it drops a normal Cauldron.

## Pipe Valve

<BlockImage id="pipe_valve" scale="3" float="right" />

The <ItemLink id="pipe_valve" /> is a decorative valve from the Rustworks plumbing. It does not control pneumatic pipes or any liquid. It is crafted from rusty plates and a rusty pipe, and it is an ingredient of the Drain Valve found in the Stalled Engine.

<RecipeFor id="pipe_valve" />

<br clear="all" />
