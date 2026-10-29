# Changelog

## Unreleased

### Versions
- Upfall now also runs on Minecraft 26.1.2 (NeoForge 26.1, Java 25) with the same content as 1.21.1.
- JEI and GuideME are supported on both versions; EMI integration is 1.21.1 only.
- Jars are now named upfall-<version>+<minecraft version>.jar.

### Gear
- New textures for the Nimbusite and Levitite ingots and tools.

### Advancements
- The Upfall advancement tab is now split into branches that follow the real dependencies: exploration, Cloud Hamlet,
  Nimbusite and Levitite gear, Rustworks machines, Chronospire, Pearl Aerie, Quantum and Tilted Reliquary.
- Seven new advancements: Glittering Depths (Crystal Grotto), Sky-Touched (Upfall enchantments), Perfect Timing (Kairite
  Gear), Clockwork (first machine), Breathe Easy (Veil Mask), Superposition (Quantum Pedestal) and Folded Space (Fold
  Shard).

### Explorer maps
- Loftkin now sell 1 or 2 random explorer maps each, paid with a Compass plus Gildfruit, Nimbusite or Levitite Shards:
  Stalled Engine, Pearl Aerie, Chronospire, Shifting Vault, Tilted Reliquary and Gilded Ridge maps.
- Maps can also be found in chests: Drifting Ruins (Chronospire, Pearl Aerie), Cloud Hamlet homes (Pearl Aerie), gas cave
  supply caches (Stalled Engine), Stalled Engine hall caches (Shifting Vault), and the Inverted Observatory, Chronospire
  vault and Pearl Aerie keep (Tilted Reliquary).

### Balance
- Every Loftkin now offers the Nimbusite Upgrade Smithing Template trade (once) instead of it being a random trade.
- Drifting Ruins hold a Nimbusite Upgrade Smithing Template 25% of the time (was 15%).
- The Rewind incantation now takes the Rewinding Hourglass and 3 Hour Dials (Chrono Gnomons could not be obtained).

### Guide
- The Progression page is rewritten as the spine of the mod: tiers in order, where to find each bottleneck, explorer
  maps and the matching advancements.

## 1.2.0

### Quantum
- Quantum objects only behave while nobody looks at them: they stay still when observed, in light or under a held light.
- Veil Gas: an invisible gas that hurts unprotected creatures. Bottle it with a glass bottle and release it on a block.
- Veil Mask, crafted at the smithing table from a Veil Mask Blueprint, an iron helmet and a Kairite Gear: filters the
  Veil Gas and reveals it.
- Gas caves: worm-like tunnels carved inside the islands and filled with Veil Gas, some dug out with Ironbark supports,
  with dead-end scenes (abandoned pickaxes, cracked masks, scattered bones, spent lanterns) and supply caches.
- Flicker Shard Clusters grow on the cave walls and jump elsewhere when unobserved; mine them while keeping an eye on
  them to get Flicker Shards.

### Shifting Vault
- A glass maze buried deep inside the islands, reached through a long tunnel.
- Its Shifting Chest moves whenever you look away. Frost Glass fogs up to block your view, the lamps go out, the floor
  crumbles under your feet and something whispers behind you.
- The chest holds the Quantum Pedestal Blueprint and Quantum Stones.

### Quantum Pedestal
- Built from a Pedestal, a Quantum Stone and its blueprint, then ringed with eight Pedestals holding the offerings.
- Place a Levitite piece on it, sneak and use it with an empty hand, then keep watching it until the incantation
  completes. Looking away makes it collapse.
- Nine upgrades for Levitite gear, up to two per piece:
  - Veilsight (helmet): filters and reveals the Veil Gas.
  - Uncertainty (chestplate): chance to dodge incoming hits.
  - Zipline (chestplate): latch onto ziplines with an empty hand.
  - Anchored (leggings): immune to knockback.
  - Solidifier (leggings): bounce on Mist, sneak to sink into it.
  - Mosswalker (boots): run much faster on moss.
  - Superposition (pickaxe): mining an ore also mines an identical neighbor.
  - Entanglement (sword): the next two creatures you hit share their wounds.
  - Rewind (any armor piece): once a day, cheats death by rewinding to the last solid ground.
- Incantation recipes are data-driven and shown in JEI and EMI when installed.

### Misc
- New advancements for gas caves, Flicker Shards, the Shifting Vault and incantations.
- Full French translation.
- Mod config: observation rules, gas damage, cave and vault generation, incantation and upgrade tuning.
- Sky Almanac: an in-game guide (GuideME) covering progression, world, creatures, structures, materials, machines
  and blocks.
- Reworked item textures.
- The Chronospire, Pearl Aerie and Crystal Grotto are rarer (roughly twice the spacing between them).

### Pearl Aerie
- New rare structure in the Drifting Archipelago: an abandoned city of the sky builders, floating between the island
  layers. A tall keep and up to seven smaller towers are linked by pearlstone bridges, some of them collapsed.
- Floors can be libraries or hanging gardens; the keep has an updraft shaft running through every floor. Long bridges
  may widen into a landing with a skywater basin or an obelisk. Cloud and mistglow lanterns light the towers and
  bridges, and glowvines and vines hang below the decks.
- Traps: weathered bricks that crumble for good under your feet, arrow traps guarding the keep treasure, and
  Duskwing and Cragmaw spawners.
- Loot: tower caches, armories with smithing templates and enchanted books, and the keep treasure.
- Gravity Sceptre: found only in the keep. Use it to become Weightless and walk on the ceiling above you; use it again
  to let go. Repaired with Levitite Ingots.
- New blocks: Cracked, Chiseled and Mossy Pearlstone Bricks (with mossy stairs, slab and wall).

### Tilted Reliquary
- New very rare structure above the cloud sea in the Drifting Archipelago, Mistsea and Gilded Ridge: a cluster of hollow
  lattice-stone cubes tilted 45 degrees and frozen mid-shatter, with no island nearby.
- Reached by an updraft column from the cloud sea, then by updraft vents, ziplines and floating beams.
  Satellite cubes hold an archive, a hanging garden, an echo hall with a Duskwing spawner, a collapsed room or a
  shard vault.
- Four Fold Shards, hidden in satellite chests, fill the four Fold Sockets outside the core's Fold Seals and dissolve them.
- Core chest: the Warden's note and the core treasure. Three lore books tell the story of the Surveyors of the Fold.
- Fold Anchor: use to set an anchor, use again within 30 seconds to snap back to it with fall distance reset. 64 uses,
  5 second cooldown, repaired with Cloud Pearls.
- No natural mob spawns inside a reliquary, and never any Hanging Roots or upside-down islands around one.
- New blocks: Lattice Stone, Carved Lattice Stone, Lattice Beam, Fold Socket and Fold Seal.

### Fold Warden
- A two-phase boss sealed in the reliquary core. It wakes up when the fourth Fold Socket is filled, and the seal
  chamber floor closes back into lattice stone as the seals dissolve.
- Phase 1, the Fold: a tilted lattice cube shielded by four Fold Satellites. Destroy them to open its shutters and
  expose its eye; they reform after 30 seconds. Attacks: Satellite Slam, Tilt (pushes everyone sideways) and Weight
  of Falling (lift then slam, a filled Fold Socket keeps you safe).
- Phase 2, the Plummet: at half health it unfolds into a wyrm of twelve lattice segments that can all be hit.
  Attacks: Coil, Shed (boomerang tail segments), Updraft Dive and Lattice Breath, which locks its target in a
  temporary Fold Lattice cage. Enraged below a quarter of its health.
- Custom boss bar for each phase and dedicated sounds.
- Drops the Fold Anchor, the new Fold Heart and 1 to 3 Fold Shards. New advancement: Unfolded.
- New Sky Almanac page.

## 1.1.0

### Rustworks
- New rare archipelago biome: Oxide Turf, Rustsoil and the Rustslate stone family under a rusty fog.
  Rustworks form large self-contained zones, cut off from the rest of the archipelago by a gap of open sky.
- Huge walkable pipes bridging islands, giant valves, half-buried cogs, pipes plunging into the cloud sea and
  leaking pipes dripping over cauldrons.
- Small, scattered islands linked by the pipe bridges.
- Flora: Rivetgrass, Verdigris Moss, glowing Verdigris Blooms and hanging Wirevines; rust flakes drift in the air.
- Ironbark trees with stacked gear-like canopies, and the full Ironbark wood set.
- Exhaust Vents burst steam and launch whatever stands above them toward the island overhead.
- Stalled automatons: wrecked Horologist machines, some hiding a Sky Cache.
- Ambience: distant clanks, drips and pipes that breathe.
- Rusty Plates (copper and Kairite Dust) craft every Rustworks metal block: plating, pipes, valves, cogs.

### Kairite
- Kairite Ore pulses slowly. Mined at its glow peak it gives a Kairite Gear, otherwise Kairite Dust (9 dust = 1 gear).
- Leaking pipes slowly fill a cauldron below with Stillflow. A full cauldron turns a Kairite Dust into a Kairite Gear.
- Kairite is out of the gear progression: it only powers utility machines.

### Machines
Built at the smithing table from a blueprint, a base item and a Kairite Gear.
- Pneumatic Pipes: sneak into an entrance to be propelled to the exit; items and hoppers work too.
- Void Tether: catches items falling into the void nearby and stores them in an adjacent container.
- Rewinder: slowly restores the durability of a tool placed on it, fueled by Kairite.
- Hastening Gear: speeds up an adjacent furnace or nearby crops, consuming Kairite.
- Phase Watch: shows the current Chronospire phase and the time until the next one.
- Gearlift: jump or sneak to travel between gearlifts in the same column.

### Stalled Engine
- New ruin in the Rustworks: collapsed pump hall, empty tanks, catwalks and a broken Heartgear.
- Chests hold the machine blueprints. Open three valves to drain the flooded vault and reach the main chest.
- Lecterns and engravings hint at a link with the Chronospire.

## 1.0.0

First release, for Minecraft 1.21.1 and NeoForge 21.1.

### The Upfall dimension
- New dimension `upfall:the_upfall` (Y -64 to 320), time frozen at dusk, with its own sky, fog and music.
- Two layers of floating islands, inverted islands with grass facing down, a cloud sea with holes onto the void and
  rare glowing stone needles rising from the abyss.
- Four biomes: Drifting Archipelago, Mistsea, Hanging Roots and Gilded Ridge.
- Skywater lakes with Skysilt shores, Skyreeds and glowing Skylilies.
- Cloud sea content: Pearled Cloud, rain patches, Cloudstalks with Cloudblooms, floating cloudlets.
- Void rescue: falling below Y -64 sends players back above the Overworld with Slow Falling. Beds work at any time.

### Access
- Halo Frame, Skyward Key and Halo Portal (frames from 4x5 to 23x23, lit only in the Overworld above Y 200).
  The return portal is always built on an island.

### Gravity
- Updraft Vents and Updraft Mist columns.
- Weightless: under an inverted island, gravity flips and players walk on its underside.
- Upfall Cascades: waterfalls rising into the underside of islands.
- Dense Cloud (soft, bouncy) and Mist, which blinds and slows.

### Structures
- Chronospire: a floating clock tower that changes shape with the Overworld time, with moving platforms, two puzzles
  and a vault holding the Rewinding Hourglass.
- Cloud Hamlet: balloon-tethered platforms joined by rope bridges and ziplines, home of the Loftkin traders.
- Crystal Grotto: a crystal geode hanging under the cloud sea.
- Drifting Ruins and Inverted Observatory, holding the smithing templates.

### Creatures
- Duskwing and Cragmaw (hostile), Mistjelly (neutral), Skygrazer (breedable), Loftkin (traders), Skyray (passive).

### Gear and items
- Nimbusite (tier 1) and Levitite (tier 2) tools, armor and upgrade templates, with full set bonuses shown in the
  tooltips (Nimbusite: -75% fall damage; Levitite: no fall damage, double jump and glide).
- Enchantments: Skywalker, Uplifted, Cloudsight and Jumpstart.
- Updraft Compass, Nimbus Flask, Rewinding Hourglass, Zipline Handle, Rope, Rope and Zipline Anchors.
- Mistglow Dust and Mistglow Lantern, Cloud Pearl and Cloud Lamp, Lumen crystals in three colors.
- Food: Gildfruit, Cloudberry, Cloudberry Tart, Cloudberry Jelly, Skygrazer meat.
- Mob drops with brewing uses: Duskwing Plume, Cragmaw Fang, Mist Gel, Down Tuft.
- Pearlstone, Duskslate, Lumen, woven and canopy building blocks, Skyreach and Gildroot wood sets.
- "Upfall" advancement tab following the whole progression.
