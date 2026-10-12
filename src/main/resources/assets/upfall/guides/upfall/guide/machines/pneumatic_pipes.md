---
navigation:
  title: Pneumatic Pipes
  icon: upfall:pneumatic_pipe
  parent: machines/index.md
  position: 70
item_ids:
  - upfall:pneumatic_pipe
  - upfall:pneumatic_pipe_entrance
  - upfall:pneumatic_pipe_exit
---

# Pneumatic Pipes

<BlockImage id="pneumatic_pipe" scale="4" float="right" />

**Pneumatic Pipes** are copper tubes pushed by air. They carry **items and players** from an Entrance to an Exit, around corners and through walls, at about 16 blocks per second.

## Parts

| Part | Role |
|---|---|
| <ItemLink id="pneumatic_pipe" /> | Connects by itself to neighbouring pipes, entrances and exits. |
| <ItemLink id="pneumatic_pipe_entrance" /> | Start of the line. Its open mouth swallows items and lets players in. |
| <ItemLink id="pneumatic_pipe_exit" /> | End of the line. Inserts or spits out what arrives. |

### Crafting

Smith a Block of Copper, a <ItemLink id="kairite_gear" /> and a [Pneumatic Pipe Blueprint](index.md) to get 8 pipes.

<RecipeFor id="pneumatic_pipe" />

Entrances and exits are a pipe plus a vanilla block (a Hopper for the Entrance, a Dropper for the Exit):

<RecipeFor id="pneumatic_pipe_entrance" />

<RecipeFor id="pneumatic_pipe_exit" />

## Building a network

Entrances and exits have a **mouth**: the side you clicked when placing them. They never connect through the mouth. Pipes connect on every side.

<GameScene zoom="3" interactive={true}>
  <IsometricCamera yaw="225" pitch="30" />
  <Block id="upfall:pneumatic_pipe_entrance" x="0" y="0" z="0" p:facing="up" />
  <Block id="upfall:pneumatic_pipe" x="1" y="0" z="0" p:west="true" p:east="true" />
  <Block id="upfall:pneumatic_pipe" x="2" y="0" z="0" p:west="true" p:east="true" />
  <Block id="upfall:pneumatic_pipe_exit" x="3" y="0" z="0" p:facing="up" />
</GameScene>

1. Place an Entrance with its mouth toward where you will drop items or step in.
2. Run pipes from the Entrance in any direction, including vertically.
3. Finish with an Exit whose mouth points at the target: a chest, a furnace, or open air.

An Entrance and an Exit can also touch each other directly. A route can span up to about 512 blocks. If several Exits are connected, the **nearest** one is used. If you break a pipe on the route, the line is cut and the Entrance stops accepting items.

## Moving items

An Entrance accepts:

- Item entities lying in front of its mouth.
- Items inserted by hoppers or other automation.

Items travel through the pipe, then the Exit tries to insert them into the inventory in front of its mouth. Whatever does not fit is blown out of the mouth. Up to 256 items can be in transit per Entrance.

## Moving players

Stand in front of an Entrance's mouth and **sneak**. You are pulled in and ride the pipe in a crawling pose to the nearest Exit that has free space in front of it, where you are shot out. You take no fall damage from the trip and cannot re-enter for a moment. If the pipe is broken during the ride, you are ejected.

If no Exit is reachable, the Entrance clunks and nothing happens.
