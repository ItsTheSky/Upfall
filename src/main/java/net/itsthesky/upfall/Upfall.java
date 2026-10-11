package net.itsthesky.upfall;

import com.mojang.logging.LogUtils;
import net.itsthesky.upfall.content.UpfallModules;
import net.itsthesky.upfall.gameplay.ArmorSetBonuses;
import net.itsthesky.upfall.gameplay.UpfallWorldEvents;
import net.itsthesky.upfall.gameplay.WeightlessZones;
import net.itsthesky.upfall.network.DoubleJumpPayload;
import net.itsthesky.upfall.registry.ModArmorMaterials;
import net.itsthesky.upfall.registry.ModBlockEntities;
import net.itsthesky.upfall.registry.ModBlocks;
import net.itsthesky.upfall.registry.ModCreativeTabs;
import net.itsthesky.upfall.registry.ModDataComponents;
import net.itsthesky.upfall.registry.ModDensityFunctions;
import net.itsthesky.upfall.registry.ModEffects;
import net.itsthesky.upfall.registry.ModEntities;
import net.itsthesky.upfall.registry.ModFeatures;
import net.itsthesky.upfall.registry.ModPlacements;
import net.itsthesky.upfall.registry.ModPoiTypes;
import net.itsthesky.upfall.registry.ModTriggers;
import net.itsthesky.upfall.registry.ModFluids;
import net.itsthesky.upfall.registry.ModItems;
import net.itsthesky.upfall.registry.ModParticles;
import net.itsthesky.upfall.registry.ModSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.slf4j.Logger;

@Mod(Upfall.MODID)
public class Upfall {
    public static final String MODID = "upfall";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Upfall(IEventBus modEventBus, ModContainer modContainer) {
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModParticles.PARTICLE_TYPES.register(modEventBus);
        ModEffects.MOB_EFFECTS.register(modEventBus);
        ModArmorMaterials.ARMOR_MATERIALS.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.CREATIVE_TABS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModDensityFunctions.DENSITY_FUNCTION_TYPES.register(modEventBus);
        ModPlacements.PLACEMENT_MODIFIERS.register(modEventBus);
        ModPoiTypes.POI_TYPES.register(modEventBus);
        ModTriggers.TRIGGERS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        UpfallModules.ALL.forEach(module -> module.register(modEventBus));
        UpfallWorldEvents.register();
        ArmorSetBonuses.register();
        WeightlessZones.register();
        modEventBus.addListener(DoubleJumpPayload::register);
        modEventBus.addListener(ModEntities::registerAttributes);
        modEventBus.addListener(ModEntities::registerSpawnPlacements);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            FlowerPotBlock pot = (FlowerPotBlock) Blocks.FLOWER_POT;
            pot.addPlant(ModBlocks.DUSKBLOOM.getId(), ModBlocks.POTTED_DUSKBLOOM);
            pot.addPlant(ModBlocks.CLOUD_FERN.getId(), ModBlocks.POTTED_CLOUD_FERN);
            pot.addPlant(ModBlocks.SKYREACH.sapling().getId(), ModBlocks.POTTED_SKYREACH_SAPLING);
            pot.addPlant(ModBlocks.IRONBARK.sapling().getId(), ModBlocks.POTTED_IRONBARK_SAPLING);

            FireBlock fire = (FireBlock) Blocks.FIRE;
            for (ModBlocks.WoodSet set : new ModBlocks.WoodSet[]{ModBlocks.SKYREACH, ModBlocks.GILDROOT, ModBlocks.IRONBARK}) {
                for (Block log : new Block[]{set.log().get(), set.wood().get(), set.strippedLog().get(), set.strippedWood().get()}) {
                    fire.setFlammable(log, 5, 5);
                }
                for (Block plank : new Block[]{set.planks().get(), set.stairs().get(), set.slab().get(), set.fence().get(), set.fenceGate().get()}) {
                    fire.setFlammable(plank, 5, 20);
                }
                fire.setFlammable(set.leaves().get(), 30, 60);
            }
            for (Block plant : new Block[]{ModBlocks.DUSKBLOOM.get(), ModBlocks.CLOUD_FERN.get(), ModBlocks.GILDED_TUFT.get()}) {
                fire.setFlammable(plant, 60, 100);
            }
        });
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
