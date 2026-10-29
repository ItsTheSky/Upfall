package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.machines.MachinesRecipes;
import net.itsthesky.upfall.content.machines.ModelBox;
import net.itsthesky.upfall.content.rustworks.RustworksBlocks;
import net.itsthesky.upfall.content.rustworks.RustworksItems;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.DataGenContext;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModFrenchLanguageProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModParticleProvider;
import net.itsthesky.upfall.datagen.ModSoundProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.itsthesky.upfall.datagen.RecipeContext;
import net.itsthesky.upfall.item.UpfallSmithingTemplates;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModTags;
import net.minecraft.Util;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class QuantumData implements DataContribution {
    static final Map<String, String[]> NAMES = new LinkedHashMap<>();

    static {
        name("veil_gas", "Veil Gas", "Gaz voilé");
        name("veil_mask", "Veil Mask", "Masque voilé");
        name("veil_mask_blueprint", "Veil Mask Blueprint", "Plan de Masque voilé");
        name("quantum_pedestal_blueprint", "Quantum Pedestal Blueprint", "Plan de Piédestal quantique");
        name("flicker_shard", "Flicker Shard", "Éclat vacillant");
        name("quantum_stone", "Quantum Stone", "Pierre quantique");
        name("bottled_veil_gas", "Bottled Veil Gas", "Fiole de gaz voilé");
        name("abandoned_pickaxe", "Abandoned Pickaxe", "Pioche abandonnée");
        name("cracked_veil_mask", "Cracked Veil Mask", "Masque voilé fissuré");
        name("scattered_bones", "Scattered Bones", "Ossements épars");
        name("spent_lantern", "Spent Lantern", "Lanterne éteinte");
        name("flicker_shard_cluster", "Flicker Shard Cluster", "Amas d'éclats vacillants");
        name("vault_bricks", "Vault Bricks", "Briques de chambre forte");
        name("vault_tiles", "Vault Tiles", "Dalles de chambre forte");
        name("vault_glass", "Vault Glass", "Verre de chambre forte");
        name("frost_glass", "Frost Glass", "Verre givrant");
        name("vault_lamp", "Vault Lamp", "Lampe de chambre forte");
        name("crumbling_floor", "Crumbling Floor", "Sol friable");
        name("shifting_chest", "Shifting Chest", "Coffre changeant");
        name("vault_core", "Vault Core", "Cœur de chambre forte");
        name("pedestal", "Pedestal", "Piédestal");
        name("quantum_pedestal", "Quantum Pedestal", "Piédestal quantique");
    }

    static void name(String path, String en, String fr) {
        NAMES.put(path, new String[]{en, fr});
    }

    @Override
    public void lang(ModLanguageProvider lang) {
        NAMES.forEach((path, names) -> {
            boolean block = QuantumBlocks.BLOCKS.getEntries().stream().anyMatch(e -> e.getId().getPath().equals(path));
            lang.add((block ? "block.upfall." : "item.upfall.") + path, names[0]);
        });
        QuantumSounds.SUBTITLES.forEach((path, text) -> lang.add(ModLanguageProvider.subtitle(path), text[0]));
        QuantumLang.english(lang::add);
        for (DeferredItem<SmithingTemplateItem> blueprint : QuantumItems.blueprints()) {
            String upgrade = blueprint.getId().getPath();
            String[] text = BLUEPRINTS.get(upgrade);
            lang.add(Util.makeDescriptionId("upgrade", Upfall.id(upgrade)), text[0]);
            lang.add(UpfallSmithingTemplates.key("applies_to", upgrade), text[1]);
            lang.add(UpfallSmithingTemplates.key("ingredients", upgrade), text[2]);
            lang.add(UpfallSmithingTemplates.key("base_slot_description", upgrade), text[3]);
            lang.add(UpfallSmithingTemplates.key("additions_slot_description", upgrade), text[4]);
        }
    }

    @Override
    public void langFr(ModFrenchLanguageProvider lang) {
        NAMES.forEach((path, names) -> {
            boolean block = QuantumBlocks.BLOCKS.getEntries().stream().anyMatch(e -> e.getId().getPath().equals(path));
            lang.add((block ? "block.upfall." : "item.upfall.") + path, names[1]);
        });
        QuantumSounds.SUBTITLES.forEach((path, text) -> lang.add(ModLanguageProvider.subtitle(path), text[1]));
        QuantumLang.french(lang::add);
        for (DeferredItem<SmithingTemplateItem> blueprint : QuantumItems.blueprints()) {
            String upgrade = blueprint.getId().getPath();
            String[] text = BLUEPRINTS_FR.get(upgrade);
            lang.add(Util.makeDescriptionId("upgrade", Upfall.id(upgrade)), text[0]);
            lang.add(UpfallSmithingTemplates.key("applies_to", upgrade), text[1]);
            lang.add(UpfallSmithingTemplates.key("ingredients", upgrade), text[2]);
            lang.add(UpfallSmithingTemplates.key("base_slot_description", upgrade), text[3]);
            lang.add(UpfallSmithingTemplates.key("additions_slot_description", upgrade), text[4]);
        }
    }

    private static final Map<String, String[]> BLUEPRINTS = Map.of(
            "veil_mask_blueprint", new String[]{"Veil Mask", "Iron Helmet", "Kairite Gear", "Add an iron helmet", "Add a Kairite Gear"},
            "quantum_pedestal_blueprint", new String[]{"Quantum Pedestal", "Pedestal", "Quantum Stone", "Add a Pedestal", "Add a Quantum Stone"});
    private static final Map<String, String[]> BLUEPRINTS_FR = Map.of(
            "veil_mask_blueprint", new String[]{"Masque voilé", "Casque en fer", "Engrenage de kairite", "Ajoutez un casque en fer", "Ajoutez un engrenage de kairite"},
            "quantum_pedestal_blueprint", new String[]{"Piédestal quantique", "Piédestal", "Pierre quantique", "Ajoutez un piédestal", "Ajoutez une pierre quantique"});

    @Override
    public void blockStates(ModBlockStateProvider states) {
        QuantumModels.blockStates(states);
    }

    static List<ModelBox> floorDecoBoxes() {
        Map<Direction, ModelBox.Face> faces = new LinkedHashMap<>();
        faces.put(Direction.UP, ModelBox.Face.of("deco").uvs(0, 0, 16, 16));
        faces.put(Direction.DOWN, ModelBox.Face.of("deco").uvs(0, 16, 16, 0));
        return List.of(new ModelBox(0, 0.25F, 0, 16, 0.25F, 16, faces));
    }

    static List<ModelBox> pedestalBoxes() {
        return List.of(
                pedestalPart(0, 0, 16, 3, Direction.DOWN),
                pedestalPart(3, 3, 13, 13, null),
                pedestalPart(1, 13, 15, 16, Direction.UP));
    }

    private static ModelBox pedestalPart(float min, float minY, float max, float maxY, Direction culled) {
        return ModelBox.cube(min, minY, min, max, maxY, max, direction -> {
            ModelBox.Face face = ModelBox.Face.of(direction == Direction.UP ? "top" : direction == Direction.DOWN ? "bottom" : "side");
            return direction == culled || direction.getAxis().isHorizontal() && min == 0 ? face.cull(direction) : face;
        });
    }

    @Override
    public void itemModels(ModItemModelProvider models) {
        models.basicItem(QuantumItems.VEIL_MASK.get());
        models.basicItem(QuantumItems.FLICKER_SHARD.get());
        models.basicItem(QuantumItems.QUANTUM_STONE.get());
        models.basicItem(QuantumItems.BOTTLED_VEIL_GAS.get());
        QuantumItems.blueprints().forEach(blueprint -> models.basicItem(blueprint.get()));
        QuantumModels.spentLanternItem(models);
    }

    @Override
    public void recipes(RecipeContext recipes) {
        MachinesRecipes.smithing(recipes.output(), "veil_mask_smithing", Ingredient.of(QuantumItems.VEIL_MASK_BLUEPRINT.get()),
                Ingredient.of(Items.IRON_HELMET), Ingredient.of(RustworksItems.KAIRITE_GEAR.get()), QuantumItems.VEIL_MASK.get(), 1);
        for (DeferredItem<SmithingTemplateItem> blueprint : QuantumItems.blueprints()) {
            recipes.shaped(RecipeCategory.MISC, blueprint.get(), 2)
                    .define('#', RustworksItems.KAIRITE_DUST.get()).define('C', RustworksBlocks.RUSTSLATE.get()).define('S', blueprint.get())
                    .pattern("#S#").pattern("#C#").pattern("###")
                    .unlockedBy(RecipeContext.hasName(blueprint.get()), recipes.has(blueprint.get()))
                    .save(recipes.output());
        }
        recipes.shaped(RecipeCategory.MISC, QuantumItems.QUANTUM_STONE.get())
                .define('S', QuantumItems.FLICKER_SHARD.get()).define('P', ModBlocks.PEARLSTONE.get())
                .pattern("SPS").pattern("P P").pattern("SPS")
                .unlockedBy(RecipeContext.hasName(QuantumItems.FLICKER_SHARD.get()), recipes.has(QuantumItems.FLICKER_SHARD.get()))
                .save(recipes.output());
        Item plate = RustworksItems.RUSTY_PLATE.get();
        recipes.shaped(RecipeCategory.DECORATIONS, QuantumBlocks.SPENT_LANTERN.get())
                .define('P', plate).define('G', Items.GLASS_PANE)
                .pattern(" P ").pattern("PGP").pattern(" P ")
                .unlockedBy(RecipeContext.hasName(plate), recipes.has(plate))
                .save(recipes.output());
        recipes.shaped(RecipeCategory.DECORATIONS, QuantumBlocks.ABANDONED_PICKAXE.get())
                .define('P', plate).define('S', Items.STICK)
                .pattern("PPP").pattern(" S ").pattern(" S ")
                .unlockedBy(RecipeContext.hasName(plate), recipes.has(plate))
                .save(recipes.output());
        QuantumRecipes.register(recipes);
    }

    @Override
    public void blockTags(ModTagProviders.Blocks tags) {
        tags.tag(ModTags.Blocks.MOSSWALKER_BLOCKS).add(Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET,
                RustworksBlocks.VERDIGRIS_MOSS.get(), RustworksBlocks.VERDIGRIS_MOSS_BLOCK.get(),
                net.itsthesky.upfall.content.grotto.GrottoBlocks.LUMEN_MOSS.get());
        tags.tag(ModTags.Blocks.GAS_CAVE_REPLACEABLE).addTag(ModTags.Blocks.UPFALL_STONE)
                .add(ModBlocks.PEARLSTONE.get(), ModBlocks.DUSKSLATE.get(), ModBlocks.SKYSOIL.get(), ModBlocks.GILDED_DUSKSLATE.get(),
                        RustworksBlocks.RUSTSLATE.get(), RustworksBlocks.RUSTSOIL.get());
        tags.tag(ModTags.Blocks.FLICKER_CLUSTER_WALLS).addTag(ModTags.Blocks.GAS_CAVE_REPLACEABLE);
        tags.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(QuantumBlocks.SPENT_LANTERN.get(), QuantumBlocks.FLICKER_SHARD_CLUSTER.get(),
                QuantumBlocks.PEDESTAL.get(), QuantumBlocks.QUANTUM_PEDESTAL.get());
        tags.tag(BlockTags.DRAGON_IMMUNE).add(vaultBlocks());
        tags.tag(BlockTags.WITHER_IMMUNE).add(vaultBlocks());
        tags.tag(BlockTags.IMPERMEABLE).add(QuantumBlocks.VAULT_GLASS.get(), QuantumBlocks.FROST_GLASS.get());
    }

    private static Block[] vaultBlocks() {
        return new Block[] {QuantumBlocks.VAULT_BRICKS.get(), QuantumBlocks.VAULT_TILES.get(), QuantumBlocks.VAULT_GLASS.get(),
                QuantumBlocks.FROST_GLASS.get(), QuantumBlocks.VAULT_LAMP.get(), QuantumBlocks.CRUMBLING_FLOOR.get(),
                QuantumBlocks.SHIFTING_CHEST.get(), QuantumBlocks.VAULT_CORE.get()};
    }

    @Override
    public void itemTags(ModTagProviders.Items tags) {
        tags.tag(ModTags.Items.VEIL_PROTECTION).add(QuantumItems.VEIL_MASK.get());
        tags.tag(ModTags.Items.HANDHELD_LIGHTS).add(Items.GLOWSTONE_DUST, Items.GLOW_BERRIES, Items.LAVA_BUCKET);
        tags.tag(ItemTags.HEAD_ARMOR).add(QuantumItems.VEIL_MASK.get());
        tags.tag(ItemTags.DURABILITY_ENCHANTABLE).add(QuantumItems.VEIL_MASK.get());
        tags.tag(ItemTags.EQUIPPABLE_ENCHANTABLE).add(QuantumItems.VEIL_MASK.get());
        tags.tag(ItemTags.HEAD_ARMOR_ENCHANTABLE).add(QuantumItems.VEIL_MASK.get());
        tags.tag(ModTags.Items.QUANTUM_UPGRADABLE).addTag(ModTags.Items.LEVITITE_ARMOR)
                .add(net.itsthesky.upfall.registry.ModItems.LEVITITE_SWORD.get(), net.itsthesky.upfall.registry.ModItems.LEVITITE_PICKAXE.get());
    }

    @Override
    public void sounds(ModSoundProvider sounds) {
        QuantumSoundDefinitions.register(sounds);
    }

    @Override
    public void particles(ModParticleProvider particles) {
        particles.addSpriteSet(QuantumParticles.VEIL_MOTE.get(), Upfall.id("veil_mote"), 2);
        particles.addSpriteSet(QuantumParticles.QUANTUM_SPARK.get(), Upfall.id("quantum_spark"), 2);
    }

    @Override
    public void structures(BootstrapContext<net.minecraft.world.level.levelgen.structure.Structure> context) {
        QuantumWorldgen.structures(context);
    }

    @Override
    public void structureSets(BootstrapContext<net.minecraft.world.level.levelgen.structure.StructureSet> context) {
        QuantumWorldgen.structureSets(context);
    }

    @Override
    public void damageTypes(BootstrapContext<DamageType> context) {
        QuantumDamageTypes.bootstrap(context);
    }

    @Override
    public void configuredCarvers(BootstrapContext<net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver<?>> context) {
        QuantumWorldgen.configuredCarvers(context);
    }

    @Override
    public void configuredFeatures(BootstrapContext<net.minecraft.world.level.levelgen.feature.ConfiguredFeature<?, ?>> context) {
        QuantumWorldgen.configuredFeatures(context);
    }

    @Override
    public void placedFeatures(BootstrapContext<net.minecraft.world.level.levelgen.placement.PlacedFeature> context) {
        QuantumWorldgen.placedFeatures(context);
    }

    @Override
    public void biomeFeatures(BiomeFeatureSink sink) {
        sink.add(QuantumWorldgen.PROPS_STEP, QuantumWorldgen.GAS_CAVE_PROPS_PLACED, QuantumWorldgen.CAVE_BIOMES);
    }

    @Override
    public void biomeCarvers(net.minecraft.resources.ResourceKey<net.minecraft.world.level.biome.Biome> biome,
                             net.minecraft.world.level.biome.BiomeGenerationSettings.Builder generation) {
        QuantumWorldgen.biomeCarvers(biome, generation);
    }

    @Override
    public void blockLoot(net.itsthesky.upfall.datagen.ModBlockLootProvider loot) {
        QuantumBlockLoot.register(loot);
    }

    @Override
    public void addProviders(DataGenContext ctx) {
        ctx.<DamageType>tags(Registries.DAMAGE_TYPE, "Quantum Damage Type Tags", tags -> {
            tags.tag(DamageTypeTags.BYPASSES_ARMOR).add(QuantumDamageTypes.VEIL_GAS, QuantumDamageTypes.ENTANGLEMENT);
            tags.tag(DamageTypeTags.NO_KNOCKBACK).add(QuantumDamageTypes.VEIL_GAS, QuantumDamageTypes.ENTANGLEMENT);
            tags.tag(DamageTypeTags.BYPASSES_SHIELD).add(QuantumDamageTypes.VEIL_GAS, QuantumDamageTypes.ENTANGLEMENT);
        });
        QuantumWorldgen.addProviders(ctx);
        ctx.add(ctx.advancements("Quantum advancements", List.of(QuantumAdvancements::generate)));
    }
}
