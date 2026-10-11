package net.itsthesky.upfall.content.hamlet;

import net.itsthesky.upfall.content.hamlet.block.RopeAnchorBlock;
import net.itsthesky.upfall.content.hamlet.block.RopeBlock;
import net.itsthesky.upfall.content.hamlet.block.RopeBridgeBlock;
import net.itsthesky.upfall.content.hamlet.block.ZiplineAnchorBlock;
import net.itsthesky.upfall.datagen.DataContribution;
import net.itsthesky.upfall.datagen.ModBlockLootProvider;
import net.itsthesky.upfall.datagen.ModBlockStateProvider;
import net.itsthesky.upfall.datagen.ModItemModelProvider;
import net.itsthesky.upfall.datagen.ModLanguageProvider;
import net.itsthesky.upfall.datagen.ModRecipeProvider;
import net.itsthesky.upfall.datagen.ModSoundProvider;
import net.itsthesky.upfall.datagen.ModTagProviders;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

final class HamletData implements DataContribution {
    @Override
    public void lang(ModLanguageProvider lang) {
        lang.add(HamletEntities.LOFTKIN.get(), "Loftkin");
        lang.add(HamletEntities.ZIPLINE_PULLEY.get(), "Zipline Pulley");
        HamletSounds.SUBTITLES.forEach((path, text) -> lang.add(ModLanguageProvider.subtitle(path), text));
        lang.add("item.upfall.rope.desc", "Climbable. Use on two anchors to tie a line.");
        lang.add("item.upfall.rope.tied", "Tied to the anchor at %s %s %s");
        lang.add("item.upfall.zipline_handle.desc", "Use on a Zipline Anchor, or near a zipline, to slide along it. Sneak to let go.");
        lang.add("message.upfall.rope.started", "Rope tied to this anchor; use it on another anchor");
        lang.add("message.upfall.rope.cancelled", "Rope untied");
        lang.add("message.upfall.rope.not_enough", "You need %s rope for a line this long");
        lang.add("message.upfall.rope.linked", "Line tied");
        lang.add("message.upfall.rope.not_anchors", "Lines can only be tied between anchors");
        lang.add("message.upfall.rope.mismatch", "A zipline must join two Zipline Anchors");
        lang.add("message.upfall.rope.too_far", "The anchors are too far apart");
        lang.add("message.upfall.rope.full", "This anchor cannot hold another line");
        lang.add("message.upfall.rope.same", "Choose another anchor");
        lang.add("message.upfall.zipline.needs_handle", "Hold a Zipline Handle to ride the line");
        lang.add("message.upfall.zipline.started", "Hold on!");
        lang.add("message.upfall.zipline.no_line", "No zipline within reach");
        lang.add("message.upfall.zipline.too_far", "Get closer to the anchor");
        lang.add("message.upfall.zipline.blocked", "Something is in the way");
        lang.add("message.upfall.zipline.busy", "You are already riding");
        lang.add("advancements.upfall.cloud_hamlet.title", "A Village in the Clouds");
        lang.add("advancements.upfall.cloud_hamlet.description", "Visit a Cloud Hamlet, hanging above the cloud sea");
        lang.add("advancements.upfall.zipline.title", "Hold On Tight");
        lang.add("advancements.upfall.zipline.description", "Slide along a zipline with a Zipline Handle");
        lang.add("advancements.upfall.loftkin_trade.title", "Rope-craft");
        lang.add("advancements.upfall.loftkin_trade.description", "Barter with a Loftkin");
    }

    @Override
    public void blockStates(ModBlockStateProvider states) {
        ResourceLocation woven = states.blockTexture("woven_planks");
        states.simpleBlockWithItem(HamletBlocks.WOVEN_PLANKS.get(), states.models().cubeAll("woven_planks", woven));
        stairsAndSlab(states, HamletBlocks.WOVEN_STAIRS.get(), HamletBlocks.WOVEN_SLAB.get(), "woven_planks", woven);
        for (HamletBlocks.CanopySet canopy : HamletBlocks.CANOPIES) {
            String name = canopy.color() + "_canopy";
            ResourceLocation texture = states.blockTexture(name);
            states.simpleBlockWithItem(canopy.block().get(), states.models().cubeAll(name, texture));
            stairsAndSlab(states, canopy.stairs().get(), canopy.slab().get(), name, texture);
        }

        states.axisBlock(HamletBlocks.ROPE_SPOOL.get(), states.blockTexture("rope_spool_side"), states.blockTexture("rope_spool_end"));
        states.blockItem(HamletBlocks.ROPE_SPOOL.get());

        rope(states);
        ropeBridge(states);
        ropeAnchor(states);
        ziplineAnchor(states);

        ModelFile lantern = states.models().withExistingParent("cloud_lantern", states.mcLoc("block/template_lantern"))
                .texture("lantern", states.blockTexture("cloud_lantern")).renderType("cutout");
        ModelFile hanging = states.models().withExistingParent("cloud_lantern_hanging", states.mcLoc("block/template_hanging_lantern"))
                .texture("lantern", states.blockTexture("cloud_lantern")).renderType("cutout");
        states.getVariantBuilder(HamletBlocks.CLOUD_LANTERN.get()).forAllStatesExcept(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(LanternBlock.HANGING) ? hanging : lantern).build(), LanternBlock.WATERLOGGED);
        flatItem(states, "cloud_lantern");
    }

    private static void stairsAndSlab(ModBlockStateProvider states, StairBlock stairs, SlabBlock slab, String full, ResourceLocation texture) {
        states.stairsBlock(stairs, texture);
        states.slabBlock(slab, states.modLoc("block/" + full), texture);
        states.blockItem(stairs);
        states.blockItem(slab);
    }

    private static void flatItem(ModBlockStateProvider states, String name) {
        states.itemModels().withExistingParent(name, states.mcLoc("item/generated")).texture("layer0", states.modLoc("item/" + name));
    }

    private static BlockModelBuilder part(ModBlockStateProvider states, String name, String texture) {
        return states.models().withExistingParent(name, states.mcLoc("block/block"))
                .texture("main", states.blockTexture(texture)).texture("particle", states.blockTexture(texture)).renderType("cutout");
    }

    private static void rope(ModBlockStateProvider states) {
        BlockModelBuilder rope = part(states, "rope", "rope");
        rope.element().from(6, 0, 6).to(10, 16, 10)
                .allFaces((dir, face) -> face.texture("#main").uvs(0, 0, 4, dir.getAxis() == Direction.Axis.Y ? 4 : 16)).end();
        BlockModelBuilder end = part(states, "rope_end", "rope");
        end.element().from(6, 5, 6).to(10, 16, 10)
                .allFaces((dir, face) -> face.texture("#main").uvs(0, 0, 4, dir.getAxis() == Direction.Axis.Y ? 4 : 11)).end();
        end.element().from(5, 1, 5).to(11, 6, 11)
                .allFaces((dir, face) -> face.texture("#main").uvs(5, dir.getAxis() == Direction.Axis.Y ? 6 : 0, 11, dir.getAxis() == Direction.Axis.Y ? 12 : 5)).end();
        end.element().from(6.5F, 0, 6.5F).to(9.5F, 1, 9.5F)
                .allFaces((dir, face) -> face.texture("#main").uvs(12, 0, 15, 1)).end();
        states.getVariantBuilder(HamletBlocks.ROPE.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(RopeBlock.END) ? end : rope).build());
        flatItem(states, "rope");
    }

    private static void ropeBridge(ModBlockStateProvider states) {
        ModelFile[] levels = new ModelFile[4];
        for (int level = 0; level < 4; level++) {
            int y = level * 4;
            BlockModelBuilder model = part(states, "rope_bridge_" + level, "rope_bridge").texture("rope", states.blockTexture("rope"));
            for (int slat = 0; slat < 4; slat++) {
                int x = slat * 4;
                model.element().from(x, y, 0).to(x + 3, y + 2, 16).allFaces((dir, face) -> {
                    face.texture("#main");
                    switch (dir.getAxis()) {
                        case Y -> face.uvs(x, 0, x + 3, 16);
                        case Z -> face.uvs(x, 0, x + 3, 2);
                        default -> face.uvs(x, 0, x + 2, 16).rotation(net.neoforged.neoforge.client.model.generators.ModelBuilder.FaceRotation.CLOCKWISE_90);
                    }
                }).end();
            }
            for (int z : new int[]{1, 14}) {
                model.element().from(0, y + 2, z).to(16, y + 3, z + 1).allFaces((dir, face) -> {
                    face.texture("#rope");
                    if (dir.getAxis() == Direction.Axis.X) {
                        face.uvs(0, 0, 1, 1);
                    } else {
                        face.uvs(1, 0, 2, 16).rotation(net.neoforged.neoforge.client.model.generators.ModelBuilder.FaceRotation.CLOCKWISE_90);
                    }
                }).end();
            }
            levels[level] = model;
        }
        states.getVariantBuilder(HamletBlocks.ROPE_BRIDGE.get()).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(levels[state.getValue(RopeBridgeBlock.LEVEL)])
                .rotationY(state.getValue(RopeBridgeBlock.AXIS) == Direction.Axis.X ? 0 : 90).build());
        flatItem(states, "rope_bridge");
    }

    private static void ropeAnchor(ModBlockStateProvider states) {
        BlockModelBuilder model = part(states, "rope_anchor", "rope_anchor");
        model.element().from(6, 0, 6).to(10, 9, 10).allFaces((dir, face) -> face.texture("#main")
                .uvs(dir.getAxis() == Direction.Axis.Y ? 11 : 0, 0, dir.getAxis() == Direction.Axis.Y ? 15 : 4, dir.getAxis() == Direction.Axis.Y ? 4 : 9)).end();
        model.element().from(5, 3, 5).to(11, 7, 11).allFaces((dir, face) -> face.texture("#main")
                .uvs(5, dir.getAxis() == Direction.Axis.Y ? 5 : 0, 11, dir.getAxis() == Direction.Axis.Y ? 11 : 4)).end();
        model.element().from(5, 9, 5).to(11, 10, 11).allFaces((dir, face) -> face.texture("#main")
                .uvs(dir.getAxis() == Direction.Axis.Y ? 6 : 0, 10, dir.getAxis() == Direction.Axis.Y ? 12 : 6, dir.getAxis() == Direction.Axis.Y ? 16 : 11)).end();
        states.getVariantBuilder(HamletBlocks.ROPE_ANCHOR.get()).forAllStates(state -> {
            Direction facing = state.getValue(RopeAnchorBlock.FACING);
            int x = facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90;
            int y = facing.getAxis().isHorizontal() ? ((int) facing.toYRot() + 180) % 360 : 0;
            return ConfiguredModel.builder().modelFile(model).rotationX(x).rotationY(y).build();
        });
        states.blockItem(HamletBlocks.ROPE_ANCHOR.get());
    }

    private static void ziplineAnchor(ModBlockStateProvider states) {
        BlockModelBuilder model = part(states, "zipline_anchor", "zipline_anchor");
        model.element().from(5, 0, 5).to(11, 9, 11).allFaces((dir, face) -> face.texture("#main")
                .uvs(0, dir.getAxis() == Direction.Axis.Y ? 10 : 0, 6, dir.getAxis() == Direction.Axis.Y ? 16 : 9)).end();
        for (int x : new int[]{5, 10}) {
            model.element().from(x, 9, 6).to(x + 1, 15, 10).allFaces((dir, face) -> face.texture("#main")
                    .uvs(7, 0, dir.getAxis() == Direction.Axis.X ? 11 : 8, dir.getAxis() == Direction.Axis.Y ? 4 : 6)).end();
        }
        model.element().from(7, 9, 5).to(9, 15, 11).allFaces((dir, face) -> {
            face.texture("#main");
            if (dir.getAxis() == Direction.Axis.X) {
                face.uvs(10, 10, 16, 16);
            } else {
                face.uvs(12, 0, 14, 6);
            }
        }).end();
        model.element().from(4.5F, 11.5F, 7.5F).to(11.5F, 12.5F, 8.5F).allFaces((dir, face) -> face.texture("#main").uvs(7, 7, 14, 8)).end();
        states.horizontalBlock(HamletBlocks.ZIPLINE_ANCHOR.get(), model);
        states.blockItem(HamletBlocks.ZIPLINE_ANCHOR.get());
    }

    @Override
    public void itemModels(ModItemModelProvider models) {
        models.basicItem(HamletItems.ZIPLINE_HANDLE.get());
        models.withExistingParent(HamletItems.LOFTKIN_SPAWN_EGG.getId().getPath(), models.mcLoc("item/template_spawn_egg"));
    }

    @Override
    public void blockLoot(ModBlockLootProvider loot) {
        loot.add(HamletBlocks.WOVEN_SLAB.get(), loot.createSlabItemTable(HamletBlocks.WOVEN_SLAB.get()));
        for (HamletBlocks.CanopySet canopy : HamletBlocks.CANOPIES) {
            loot.add(canopy.slab().get(), loot.createSlabItemTable(canopy.slab().get()));
        }
    }

    @Override
    public void recipes(RecipeOutput output) {
        shaped(output, HamletBlocks.WOVEN_PLANKS.get(), 4, ModBlocks.SKYREACH.planks().get(), List.of("PS", "SP"), 'P', ModBlocks.SKYREACH.planks().get(), 'S', Items.STRING);
        stairs(output, HamletBlocks.WOVEN_STAIRS.get(), HamletBlocks.WOVEN_PLANKS.get());
        slab(output, HamletBlocks.WOVEN_SLAB.get(), HamletBlocks.WOVEN_PLANKS.get());
        ItemLike[] wool = {Items.PINK_WOOL, Items.WHITE_WOOL, Items.LIGHT_BLUE_WOOL};
        for (int i = 0; i < HamletBlocks.CANOPIES.size(); i++) {
            HamletBlocks.CanopySet canopy = HamletBlocks.CANOPIES.get(i);
            shaped(output, canopy.block().get(), 4, wool[i], List.of("WW", "WW"), 'W', wool[i]);
            stairs(output, canopy.stairs().get(), canopy.block().get());
            slab(output, canopy.slab().get(), canopy.block().get());
        }
        shaped(output, HamletBlocks.ROPE_ITEM.get(), 3, Items.STRING, List.of("S", "S", "S"), 'S', Items.STRING);
        shaped(output, HamletBlocks.ROPE_BRIDGE.get(), 4, HamletBlocks.ROPE_ITEM.get(), List.of("R R", "SSS"), 'R', HamletBlocks.ROPE_ITEM.get(), 'S', HamletBlocks.WOVEN_SLAB.get());
        shaped(output, HamletBlocks.ROPE_ANCHOR.get(), 2, HamletBlocks.ROPE_ITEM.get(), List.of("R", "T"), 'R', HamletBlocks.ROPE_ITEM.get(), 'T', Items.STICK);
        shaped(output, HamletBlocks.ZIPLINE_ANCHOR.get(), 2, ModItems.NIMBUSITE_INGOT.get(), List.of(" N ", "RLR"), 'N', ModItems.NIMBUSITE_INGOT.get(),
                'R', HamletBlocks.ROPE_ITEM.get(), 'L', ModBlocks.SKYREACH.log().get());
        shaped(output, HamletItems.ZIPLINE_HANDLE.get(), 1, ModItems.NIMBUSITE_INGOT.get(), List.of(" N ", "T T"), 'N', ModItems.NIMBUSITE_INGOT.get(), 'T', Items.STICK);
        shaped(output, HamletBlocks.CLOUD_LANTERN.get(), 1, ModBlocks.MISTGLOW_CRYSTAL.get(), List.of("GGG", "GMG", "GGG"), 'G', Items.GOLD_NUGGET,
                'M', ModBlocks.MISTGLOW_CRYSTAL.get());
        shaped(output, HamletBlocks.ROPE_SPOOL.get(), 1, HamletBlocks.ROPE_ITEM.get(), List.of("RRR", "RLR", "RRR"), 'R', HamletBlocks.ROPE_ITEM.get(),
                'L', ModBlocks.SKYREACH.log().get());
    }

    private static void shaped(RecipeOutput output, ItemLike result, int count, ItemLike unlock, List<String> pattern, Object... keys) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(result == HamletItems.ZIPLINE_HANDLE.get() ? RecipeCategory.TOOLS : RecipeCategory.BUILDING_BLOCKS, result, count);
        pattern.forEach(builder::pattern);
        for (int i = 0; i < keys.length; i += 2) {
            builder.define((Character) keys[i], (ItemLike) keys[i + 1]);
        }
        builder.unlockedBy(ModRecipeProvider.hasName(unlock), ModRecipeProvider.hasItem(unlock)).save(output);
    }

    private static void stairs(RecipeOutput output, Block stairs, Block base) {
        shaped(output, stairs, 4, base, List.of("#  ", "## ", "###"), '#', base);
    }

    private static void slab(RecipeOutput output, Block slab, Block base) {
        shaped(output, slab, 6, base, List.of("###"), '#', base);
    }

    @Override
    public void blockTags(ModTagProviders.Blocks tags) {
        tags.tag(BlockTags.CLIMBABLE).add(HamletBlocks.ROPE.get());
        tags.tag(BlockTags.MINEABLE_WITH_AXE).add(HamletBlocks.WOVEN_PLANKS.get(), HamletBlocks.WOVEN_STAIRS.get(), HamletBlocks.WOVEN_SLAB.get(),
                HamletBlocks.ROPE_BRIDGE.get(), HamletBlocks.ROPE_ANCHOR.get(), HamletBlocks.ZIPLINE_ANCHOR.get(), HamletBlocks.ROPE_SPOOL.get());
        tags.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(HamletBlocks.CLOUD_LANTERN.get());
        tags.tag(BlockTags.PLANKS).add(HamletBlocks.WOVEN_PLANKS.get());
        tags.tag(BlockTags.WOODEN_STAIRS).add(HamletBlocks.WOVEN_STAIRS.get());
        tags.tag(BlockTags.WOODEN_SLABS).add(HamletBlocks.WOVEN_SLAB.get());
        for (HamletBlocks.CanopySet canopy : HamletBlocks.CANOPIES) {
            tags.tag(BlockTags.STAIRS).add(canopy.stairs().get());
            tags.tag(BlockTags.SLABS).add(canopy.slab().get());
        }
    }

    @Override
    public void itemTags(ModTagProviders.Items tags) {
        tags.tag(ItemTags.PLANKS).add(HamletBlocks.WOVEN_PLANKS.asItem());
        tags.tag(ItemTags.WOODEN_STAIRS).add(HamletBlocks.WOVEN_STAIRS.asItem());
        tags.tag(ItemTags.WOODEN_SLABS).add(HamletBlocks.WOVEN_SLAB.asItem());
        for (HamletBlocks.CanopySet canopy : HamletBlocks.CANOPIES) {
            tags.tag(ItemTags.STAIRS).add(canopy.stairs().asItem());
            tags.tag(ItemTags.SLABS).add(canopy.slab().asItem());
        }
    }

    @Override
    public void sounds(ModSoundProvider sounds) {
        sounds.addSound(HamletSounds.LOFTKIN_AMBIENT, ModSoundProvider.def(HamletSounds.LOFTKIN_AMBIENT)
                .with(ModSoundProvider.event("entity.allay.ambient_without_item").pitch(0.7).volume(0.6),
                        ModSoundProvider.event("entity.wandering_trader.ambient").pitch(1.5).volume(0.4)));
        sounds.addSound(HamletSounds.LOFTKIN_HURT, ModSoundProvider.def(HamletSounds.LOFTKIN_HURT)
                .with(ModSoundProvider.event("entity.allay.hurt").pitch(0.7)));
        sounds.addSound(HamletSounds.LOFTKIN_DEATH, ModSoundProvider.def(HamletSounds.LOFTKIN_DEATH)
                .with(ModSoundProvider.event("entity.allay.death").pitch(0.7)));
        sounds.addSound(HamletSounds.LOFTKIN_YES, ModSoundProvider.def(HamletSounds.LOFTKIN_YES)
                .with(ModSoundProvider.event("entity.allay.item_given").pitch(0.8)));
        sounds.addSound(HamletSounds.LOFTKIN_NO, ModSoundProvider.def(HamletSounds.LOFTKIN_NO)
                .with(ModSoundProvider.event("entity.allay.item_taken").pitch(0.55)));
        sounds.addSound(HamletSounds.LOFTKIN_GREET, ModSoundProvider.def(HamletSounds.LOFTKIN_GREET)
                .with(ModSoundProvider.event("entity.allay.ambient_with_item").pitch(0.9).volume(0.7),
                        ModSoundProvider.event("block.amethyst_block.chime").pitch(1.2).volume(0.5)));
        sounds.addSound(HamletSounds.LOFTKIN_PARASOL, ModSoundProvider.def(HamletSounds.LOFTKIN_PARASOL)
                .with(ModSoundProvider.event("item.armor.equip_elytra").pitch(1.4)));
        sounds.addSound(HamletSounds.ZIPLINE_ATTACH, ModSoundProvider.def(HamletSounds.ZIPLINE_ATTACH)
                .with(ModSoundProvider.event("block.chain.place").pitch(1.4)));
        sounds.addSound(HamletSounds.ZIPLINE_RIDE, ModSoundProvider.def(HamletSounds.ZIPLINE_RIDE)
                .with(ModSoundProvider.event("entity.minecart.riding").pitch(1.7)));
        sounds.addSound(HamletSounds.ZIPLINE_DETACH, ModSoundProvider.def(HamletSounds.ZIPLINE_DETACH)
                .with(ModSoundProvider.event("block.chain.hit").pitch(1.2)));
        sounds.addSound(HamletSounds.ROPE_TIE, ModSoundProvider.def(HamletSounds.ROPE_TIE)
                .with(ModSoundProvider.event("entity.leash_knot.place").pitch(0.9), ModSoundProvider.event("block.wool.place").pitch(0.8)));
    }

    @Override
    public void structures(BootstrapContext<Structure> context) {
        HamletStructures.bootstrapStructures(context);
    }

    @Override
    public void structureSets(BootstrapContext<StructureSet> context) {
        HamletStructures.bootstrapSets(context);
    }

    @Override
    public void addProviders(GatherDataEvent event, DataGenerator generator, PackOutput output,
                             CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper helper) {
        generator.addProvider(event.includeServer(), new Named("Cloud Hamlet loot tables", new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(HamletLootProvider::new, LootContextParamSets.CHEST)), registries)));
        generator.addProvider(event.includeServer(), new Named("Cloud Hamlet advancements",
                new AdvancementProvider(output, registries, helper, List.of(new HamletAdvancements()))));
    }

    private record Named(String name, DataProvider delegate) implements DataProvider {
        @Override
        public CompletableFuture<?> run(CachedOutput output) {
            return delegate.run(output);
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
