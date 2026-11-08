package net.itsthesky.upfall;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class UpfallConfig {
    private UpfallConfig() {}

    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.IntValue OBSERVATION_RANGE;
    public static final ModConfigSpec.DoubleValue OBSERVATION_FOV_DOT;
    public static final ModConfigSpec.IntValue OBSERVATION_MIN_LIGHT;
    public static final ModConfigSpec.IntValue OBSERVATION_HANDHELD_LIGHT_RANGE;
    public static final ModConfigSpec.IntValue OBSERVATION_INTERVAL;

    public static final ModConfigSpec.DoubleValue GAS_DAMAGE;
    public static final ModConfigSpec.IntValue GAS_DAMAGE_INTERVAL;

    public static final ModConfigSpec.BooleanValue CAVES_ENABLED;
    public static final ModConfigSpec.DoubleValue CAVES_NETWORK_CHANCE;
    public static final ModConfigSpec.IntValue CAVES_MIN_Y;
    public static final ModConfigSpec.IntValue CAVES_GRID_SIZE;
    public static final ModConfigSpec.IntValue CAVES_ROOMS;
    public static final ModConfigSpec.DoubleValue CAVES_ROOM_RADIUS;
    public static final ModConfigSpec.IntValue CAVES_SHELL;
    public static final ModConfigSpec.DoubleValue CAVES_LOOPS;
    public static final ModConfigSpec.DoubleValue CAVES_SINUOSITY;
    public static final ModConfigSpec.DoubleValue CAVES_CORRIDOR_RADIUS;
    public static final ModConfigSpec.DoubleValue CAVES_ENTRANCE_RADIUS;
    public static final ModConfigSpec.DoubleValue CAVES_MARKER_CHANCE;
    public static final ModConfigSpec.DoubleValue CAVES_CLUSTER_CHANCE;

    public static final ModConfigSpec.DoubleValue CLUSTER_JUMP_CHANCE;
    public static final ModConfigSpec.IntValue CLUSTER_JUMP_RADIUS;

    public static final ModConfigSpec.IntValue VAULT_SPACING;
    public static final ModConfigSpec.IntValue VAULT_SEPARATION;
    public static final ModConfigSpec.IntValue VAULT_MARGIN;
    public static final ModConfigSpec.IntValue VAULT_TUNNEL_MIN;
    public static final ModConfigSpec.IntValue VAULT_TUNNEL_MAX;
    public static final ModConfigSpec.IntValue VAULT_FROST_TICKS;
    public static final ModConfigSpec.IntValue VAULT_LIGHTS_OUT_TICKS;
    public static final ModConfigSpec.IntValue VAULT_WHISPER_INTERVAL;
    public static final ModConfigSpec.IntValue VAULT_FLOOR_REGROW_TICKS;

    public static final ModConfigSpec.IntValue PEDESTAL_RING_DISTANCE;
    public static final ModConfigSpec.IntValue RITUAL_TICKS;
    public static final ModConfigSpec.IntValue MAX_UPGRADES;

    public static final ModConfigSpec.DoubleValue UNCERTAINTY_CHANCE;
    public static final ModConfigSpec.IntValue ENTANGLEMENT_TICKS;
    public static final ModConfigSpec.DoubleValue ENTANGLEMENT_SHARE;
    public static final ModConfigSpec.DoubleValue SOLIDIFIER_FACTOR;
    public static final ModConfigSpec.DoubleValue SOLIDIFIER_MIN_VELOCITY;
    public static final ModConfigSpec.DoubleValue MOSSWALKER_SPEED;
    public static final ModConfigSpec.DoubleValue REWIND_HEALTH;
    public static final ModConfigSpec.IntValue REWIND_COOLDOWN;

    public static final ModConfigSpec.DoubleValue SKYBOUND_MOB_HEALTH;
    public static final ModConfigSpec.DoubleValue SKYBOUND_MOB_DAMAGE;
    public static final ModConfigSpec.BooleanValue SKYBOUND_RECIPE_PACK;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.comment("Shared observation rule used by every quantum object").push("observation");
        OBSERVATION_RANGE = b.comment("Max distance (blocks) at which a player can observe a quantum object")
                .defineInRange("range", 64, 4, 256);
        OBSERVATION_FOV_DOT = b.comment("Minimum dot product between the look direction and the direction to the object (1 = dead center)")
                .defineInRange("fovDot", 0.6, -1.0, 1.0);
        OBSERVATION_MIN_LIGHT = b.comment("Minimum light level at the object for it to be seen without a handheld light")
                .defineInRange("minLight", 4, 0, 15);
        OBSERVATION_HANDHELD_LIGHT_RANGE = b.comment("Max distance at which a light held by the observer lights the object")
                .defineInRange("handheldLightRange", 64, 0, 256);
        OBSERVATION_INTERVAL = b.comment("Ticks between two observation checks")
                .defineInRange("interval", 4, 1, 40);
        b.pop();

        b.push("veil_gas");
        GAS_DAMAGE = b.comment("Damage dealt by Veil Gas to unprotected entities (2 = one heart)")
                .defineInRange("damage", 2.0, 0.0, 100.0);
        GAS_DAMAGE_INTERVAL = b.comment("Ticks between two Veil Gas damage ticks")
                .defineInRange("damageInterval", 20, 1, 200);
        b.pop();

        b.comment("Gas caves carved inside the islands (world generation)").push("gas_caves");
        CAVES_ENABLED = b.define("enabled", true);
        CAVES_NETWORK_CHANCE = b.comment("Chance for each region (gridSize + 32 blocks wide) to hold a cave network")
                .defineInRange("networkChance", 0.6, 0.0, 1.0);
        CAVES_MIN_Y = b.defineInRange("minY", 72, -64, 320);
        CAVES_GRID_SIZE = b.comment("Width in blocks of the area sampled around a network; caves never reach past it")
                .defineInRange("gridSize", 96, 48, 160);
        CAVES_ROOMS = b.comment("Max rooms per network")
                .defineInRange("rooms", 12, 1, 64);
        CAVES_ROOM_RADIUS = b.comment("Base horizontal radius of a room")
                .defineInRange("roomRadius", 5.5, 2.0, 16.0);
        CAVES_SHELL = b.comment("Min rock depth kept between a room and the island surface")
                .defineInRange("shell", 3, 1, 16);
        CAVES_LOOPS = b.comment("Extra corridors on top of the spanning tree, as a fraction of the room count")
                .defineInRange("loops", 0.05, 0.0, 2.0);
        CAVES_SINUOSITY = b.comment("Sideways wander of corridors, 0 for straight")
                .defineInRange("sinuosity", 0.05, 0.0, 1.0);
        CAVES_CORRIDOR_RADIUS = b.defineInRange("corridorRadius", 1.1, 0.8, 4.0);
        CAVES_ENTRANCE_RADIUS = b.comment("Base radius of the cliff, chimney and spiral entrances")
                .defineInRange("entranceRadius", 2.5, 1.0, 6.0);
        CAVES_MARKER_CHANCE = b.comment("Chance for a rock block around an entrance to become Mistglow Crystal")
                .defineInRange("markerChance", 0.25, 0.0, 1.0);
        CAVES_CLUSTER_CHANCE = b.comment("Chance per room or corridor block to grow a Flicker Shard Cluster on an adjacent wall")
                .defineInRange("clusterChance", 0.01, 0.0, 1.0);
        b.pop();

        b.push("flicker_shard_cluster");
        CLUSTER_JUMP_CHANCE = b.comment("Chance per observation check for an unobserved cluster to jump elsewhere")
                .defineInRange("jumpChance", 0.15, 0.0, 1.0);
        CLUSTER_JUMP_RADIUS = b.defineInRange("jumpRadius", 4, 1, 16);
        b.pop();

        b.push("shifting_vault");
        VAULT_SPACING = b.comment("Structure set spacing in chunks (restart required)")
                .defineInRange("spacing", 56, 8, 4096);
        VAULT_SEPARATION = b.comment("Structure set separation in chunks, must be lower than spacing (restart required)")
                .defineInRange("separation", 24, 1, 4095);
        VAULT_MARGIN = b.comment("Solid terrain margin required around the vault on every side")
                .defineInRange("margin", 7, 2, 16);
        VAULT_TUNNEL_MIN = b.defineInRange("tunnelMinLength", 60, 10, 200);
        VAULT_TUNNEL_MAX = b.defineInRange("tunnelMaxLength", 120, 10, 200);
        VAULT_FROST_TICKS = b.comment("Ticks a frosted pane stays opaque").defineInRange("frostTicks", 100, 1, 2400);
        VAULT_LIGHTS_OUT_TICKS = b.defineInRange("lightsOutTicks", 140, 1, 2400);
        VAULT_WHISPER_INTERVAL = b.comment("Average ticks between two lure sounds behind a player")
                .defineInRange("whisperInterval", 160, 20, 24000);
        VAULT_FLOOR_REGROW_TICKS = b.defineInRange("crumblingFloorRegrowTicks", 200, 20, 24000);
        b.pop();

        b.push("quantum_pedestal");
        PEDESTAL_RING_DISTANCE = b.comment("Distance between the Quantum Pedestal and its ring of Pedestals")
                .defineInRange("ringDistance", 3, 2, 8);
        RITUAL_TICKS = b.comment("Ticks the player must keep observing the Quantum Pedestal")
                .defineInRange("ritualTicks", 100, 1, 2400);
        MAX_UPGRADES = b.defineInRange("maxUpgradesPerItem", 2, 1, 9);
        b.pop();

        b.push("upgrades");
        UNCERTAINTY_CHANCE = b.defineInRange("uncertaintyDodgeChance", 0.15, 0.0, 1.0);
        ENTANGLEMENT_TICKS = b.defineInRange("entanglementTicks", 200, 1, 12000);
        ENTANGLEMENT_SHARE = b.defineInRange("entanglementShare", 0.5, 0.0, 1.0);
        SOLIDIFIER_FACTOR = b.comment("Bounce restitution k: new vertical speed = max(minVelocity, |impact| * k)")
                .defineInRange("solidifierFactor", 0.85, 0.0, 1.5);
        SOLIDIFIER_MIN_VELOCITY = b.comment("Minimum bounce speed (0.62 is about a 2.5 block bounce)")
                .defineInRange("solidifierMinVelocity", 0.62, 0.0, 3.0);
        MOSSWALKER_SPEED = b.comment("Movement speed added on moss (base player speed is 0.1)")
                .defineInRange("mosswalkerSpeed", 0.08, 0.0, 1.0);
        REWIND_HEALTH = b.comment("Fraction of max health restored by Rewind")
                .defineInRange("rewindHealth", 0.3, 0.05, 1.0);
        REWIND_COOLDOWN = b.comment("Cooldown of Rewind in ticks (24000 = one game day)")
                .defineInRange("rewindCooldown", 24000, 0, 1000000);
        b.pop();

        b.comment("Worlds created with the \"The Upfall\" world preset").push("skybound");
        SKYBOUND_MOB_HEALTH = b.comment("Max health multiplier applied to hostile mobs (bosses excluded)")
                .defineInRange("mobHealthMultiplier", 0.2, 0.05, 1.0);
        SKYBOUND_MOB_DAMAGE = b.comment("Damage multiplier applied to attacks from hostile mobs (bosses excluded)")
                .defineInRange("mobDamageMultiplier", 0.2, 0.0, 1.0);
        SKYBOUND_RECIPE_PACK = b.comment("Enable the built-in recipe pack that removes Nether and End requirements when such a world starts")
                .define("enableRecipePack", true);
        b.pop();

        SPEC = b.build();
    }

    public static <T> T get(ModConfigSpec.ConfigValue<T> value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    public static int getInt(ModConfigSpec.IntValue value) {
        return get(value);
    }

    public static double getDouble(ModConfigSpec.DoubleValue value) {
        return get(value);
    }
}
