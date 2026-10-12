package net.itsthesky.upfall.registry;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

public final class ModTags {
    private ModTags() {}

    public static final class Blocks {
        public static final TagKey<Block> SKY_PLANTABLE = tag("sky_plantable");
        public static final TagKey<Block> UPFALL_STONE = tag("upfall_stone");
        public static final TagKey<Block> NEEDS_NIMBUSITE_TOOL = tag("needs_nimbusite_tool");
        public static final TagKey<Block> INCORRECT_FOR_NIMBUSITE_TOOL = tag("incorrect_for_nimbusite_tool");
        public static final TagKey<Block> INCORRECT_FOR_LEVITITE_TOOL = tag("incorrect_for_levitite_tool");
        public static final TagKey<Block> SKYREACH_LOGS = tag("skyreach_logs");
        public static final TagKey<Block> GILDROOT_LOGS = tag("gildroot_logs");
        public static final TagKey<Block> IRONBARK_LOGS = tag("ironbark_logs");
        public static final TagKey<Block> FEATURE_REPLACEABLE = tag("feature_replaceable");
        public static final TagKey<Block> HALO_PORTAL_FRAMES = tag("halo_portal_frames");
        public static final TagKey<Block> MOSSWALKER_BLOCKS = tag("mosswalker_blocks");
        public static final TagKey<Block> GAS_CAVE_REPLACEABLE = tag("gas_cave_replaceable");
        public static final TagKey<Block> FLICKER_CLUSTER_WALLS = tag("flicker_cluster_walls");

        private static TagKey<Block> tag(String name) {
            return TagKey.create(Registries.BLOCK, Upfall.id(name));
        }
    }

    public static final class Items {
        public static final TagKey<Item> SKYREACH_LOGS = tag("skyreach_logs");
        public static final TagKey<Item> GILDROOT_LOGS = tag("gildroot_logs");
        public static final TagKey<Item> IRONBARK_LOGS = tag("ironbark_logs");
        public static final TagKey<Item> NIMBUSITE_ARMOR = tag("nimbusite_armor");
        public static final TagKey<Item> LEVITITE_ARMOR = tag("levitite_armor");
        public static final TagKey<Item> HANDHELD_LIGHTS = tag("handheld_lights");
        public static final TagKey<Item> VEIL_PROTECTION = tag("veil_protection");
        public static final TagKey<Item> QUANTUM_UPGRADABLE = tag("quantum_upgradable");

        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM, Upfall.id(name));
        }
    }

    public static final class Biomes {
        public static final TagKey<Biome> IS_UPFALL = tag("is_upfall");

        private static TagKey<Biome> tag(String name) {
            return TagKey.create(Registries.BIOME, Upfall.id(name));
        }
    }
}
