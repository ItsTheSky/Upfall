---
navigation:
  title: Machines & Quantum
  icon: upfall:kairite_gear
  position: 60
item_ids:
  - upfall:pneumatic_pipe_blueprint
  - upfall:void_tether_blueprint
  - upfall:rewinder_blueprint
  - upfall:hastening_gear_blueprint
  - upfall:phase_watch_blueprint
  - upfall:gearlift_blueprint
  - upfall:veil_mask_blueprint
  - upfall:quantum_pedestal_blueprint
---

# Machines & Quantum

The Upfall has two kinds of devices. **Clockwork machines** are built from salvaged plans and run on <ItemLink id="kairite_gear" /> or <ItemLink id="kairite_dust" />. **Quantum devices** only work while somebody is watching them.

<SubPages icons={true} />

## Blueprints

You cannot craft a machine from scratch. Each one needs a **blueprint**, a smithing template that holds the plans.

<ItemGrid>
  <ItemIcon id="gearlift_blueprint" />
  <ItemIcon id="hastening_gear_blueprint" />
  <ItemIcon id="phase_watch_blueprint" />
  <ItemIcon id="pneumatic_pipe_blueprint" />
  <ItemIcon id="rewinder_blueprint" />
  <ItemIcon id="void_tether_blueprint" />
  <ItemIcon id="veil_mask_blueprint" />
  <ItemIcon id="quantum_pedestal_blueprint" />
</ItemGrid>

### Using a blueprint

Open a smithing table and fill the three slots. The blueprint is **consumed** by each smithing operation, exactly like a vanilla smithing template.

| Blueprint | Base | Addition | Result |
|---|---|---|---|
| Gearlift Blueprint | Piston | <ItemLink id="kairite_gear" /> | 2 <ItemLink id="gearlift" /> |
| Hastening Gear Blueprint | Block of Redstone | <ItemLink id="kairite_gear" /> | <ItemLink id="hastening_gear" /> |
| Phase Watch Blueprint | Clock | <ItemLink id="kairite_gear" /> | <ItemLink id="phase_watch" /> |
| Pneumatic Pipe Blueprint | Block of Copper | <ItemLink id="kairite_gear" /> | 8 <ItemLink id="pneumatic_pipe" /> |
| Rewinder Blueprint | Anvil | <ItemLink id="kairite_gear" /> | <ItemLink id="rewinder" /> |
| Void Tether Blueprint | Ender Pearl | <ItemLink id="kairite_gear" /> | <ItemLink id="void_tether" /> |
| Veil Mask Blueprint | Iron Helmet | <ItemLink id="kairite_gear" /> | <ItemLink id="veil_mask" /> |
| Quantum Pedestal Blueprint | <ItemLink id="pedestal" /> | <ItemLink id="quantum_stone" /> | <ItemLink id="quantum_pedestal" /> |

### Where to find blueprints

- The **Stalled Engine**, the ruined pump hall of the Rustworks, holds blueprints in its chests. The sealed vault gives one or two clockwork blueprints per visit. The Veil Mask Blueprint is the most common find in the main hall.
- The wrecked **Stalled Automatons** of the Rustworks can carry a clockwork blueprint, though most hold nothing.
- The **Shifting Vault** always holds a Quantum Pedestal Blueprint.

The Rewinder, Void Tether and Hastening Gear plans are rarer than the Phase Watch, Gearlift and Pneumatic Pipe ones.

### Copying a blueprint

Once you own a blueprint you never have to find it again. Craft it together with seven <ItemLink id="kairite_dust" /> and a <ItemLink id="rustslate" /> to get two copies:

<RecipeFor id="gearlift_blueprint" />

The same pattern works for every blueprint in this chapter.

## Fuel

Hastening Gear and Rewinder burn Kairite. A <ItemLink id="kairite_dust" /> is worth 9 units and a <ItemLink id="kairite_gear" /> is worth 81 units (nine dust craft one gear). Right-click the machine with either item to refuel it.
