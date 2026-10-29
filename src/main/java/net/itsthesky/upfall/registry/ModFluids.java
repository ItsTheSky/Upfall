package net.itsthesky.upfall.registry;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.compat.mc.McRegistration;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathType;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public final class ModFluids {
    private ModFluids() {}

    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, Upfall.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, Upfall.MODID);

    public static final DeferredHolder<FluidType, FluidType> SKYWATER_TYPE = FLUID_TYPES.register("skywater", () -> new FluidType(
            FluidType.Properties.create()
                    .descriptionId("block.upfall.skywater")
                    .canSwim(true).canDrown(true).canExtinguish(true).canConvertToSource(true).supportsBoating(true).canHydrate(true)
                    .fallDistanceModifier(0.0F).motionScale(0.014).pathType(PathType.WATER).adjacentPathType(null)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
                    .sound(SoundActions.FLUID_VAPORIZE, SoundEvents.FIRE_EXTINGUISH)));

    public static final DeferredHolder<Fluid, FlowingFluid> SKYWATER = FLUIDS.register("skywater",
            () -> new BaseFlowingFluid.Source(properties()));
    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_SKYWATER = FLUIDS.register("flowing_skywater",
            () -> new BaseFlowingFluid.Flowing(properties()));

    public static final DeferredBlock<LiquidBlock> SKYWATER_BLOCK = McRegistration.block(ModBlocks.BLOCKS, "skywater",
            props -> new LiquidBlock(SKYWATER.get(), props), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .pushReaction(PushReaction.DESTROY));

    private static BaseFlowingFluid.Properties properties() {
        return new BaseFlowingFluid.Properties(SKYWATER_TYPE, SKYWATER, FLOWING_SKYWATER)
                .bucket(ModItems.SKYWATER_BUCKET).block(SKYWATER_BLOCK).slopeFindDistance(4).levelDecreasePerBlock(1).tickRate(5);
    }
}
