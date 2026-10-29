package net.itsthesky.upfall.block;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.lighting.LightEngine;

public class SkygrassBlock extends Block implements BonemealableBlock {
    public static final MapCodec<SkygrassBlock> CODEC = simpleCodec(SkygrassBlock::new);
    public static final BooleanProperty INVERTED = BooleanProperty.create("inverted");

    public SkygrassBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(INVERTED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INVERTED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(INVERTED, context.getClickedFace() == Direction.DOWN);
    }

    public static Direction grassFace(BlockState state) {
        return state.getValue(INVERTED) ? Direction.DOWN : Direction.UP;
    }

    private static boolean canBeGrass(BlockState state, LevelReader level, BlockPos pos) {
        Direction face = grassFace(state);
        BlockPos facing = pos.relative(face);
        BlockState facingState = level.getBlockState(facing);
        if (facingState.getFluidState().getAmount() == 8) {
            return false;
        }
        //? if >=26.1 {
        /*int opacity = LightEngine.getLightBlockInto(state, facingState, face, facingState.getLightDampening());
        return opacity < LightEngine.MAX_LEVEL;
        *///?} else {
        int opacity = LightEngine.getLightBlockInto(level, state, pos, facingState, facing, face, facingState.getLightBlock(level, facing));
        return opacity < level.getMaxLightLevel();
        //?}
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!canBeGrass(state, level, pos)) {
            if (!level.isAreaLoaded(pos, 1)) return;
            level.setBlockAndUpdate(pos, ModBlocks.SKYSOIL.get().defaultBlockState());
            return;
        }
        if (!level.isAreaLoaded(pos, 3)) return;
        for (int i = 0; i < 4; i++) {
            BlockPos target = pos.offset(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
            if (!level.getBlockState(target).is(ModBlocks.SKYSOIL.get())) continue;
            BlockState spread = defaultBlockState().setValue(INVERTED, state.getValue(INVERTED));
            if (canBeGrass(spread, level, target)) {
                level.setBlockAndUpdate(target, spread);
            }
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return level.getBlockState(pos.relative(grassFace(state))).isAir();
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        Direction face = grassFace(state);
        for (int i = 0; i < 48; i++) {
            BlockPos ground = pos.offset(random.nextInt(7) - 3, random.nextInt(3) - 1, random.nextInt(7) - 3);
            BlockState groundState = level.getBlockState(ground);
            if (!groundState.is(this) || groundState.getValue(INVERTED) != state.getValue(INVERTED)) continue;
            BlockPos target = ground.relative(face);
            if (!level.getBlockState(target).isAir()) continue;
            BlockState plant = pickPlant(random, face);
            if (plant.canSurvive(level, target)) {
                level.setBlock(target, plant, Block.UPDATE_ALL);
            }
        }
    }

    private static BlockState pickPlant(RandomSource random, Direction face) {
        if (face == Direction.DOWN) {
            return ModBlocks.GLOWVINE.get().defaultBlockState();
        }
        int roll = random.nextInt(10);
        if (roll < 5) return ModBlocks.GILDED_TUFT.get().defaultBlockState();
        if (roll < 8) return ModBlocks.CLOUD_FERN.get().defaultBlockState();
        return ModBlocks.DUSKBLOOM.get().defaultBlockState();
    }

    @Override
    public Type getType() {
        return Type.NEIGHBOR_SPREADER;
    }
}
