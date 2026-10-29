package net.itsthesky.upfall.content.reliquary.block;

import net.itsthesky.upfall.compat.mc.McPlayers;
import net.itsthesky.upfall.compat.mc.UseResults;
import net.itsthesky.upfall.content.quantum.QuantumParticles;
import net.itsthesky.upfall.content.reliquary.ReliquaryBlocks;
import net.itsthesky.upfall.content.reliquary.ReliquaryItems;
import net.itsthesky.upfall.content.reliquary.boss.FoldWarden;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.BlockHitResult;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
//? if <26.1 {
import net.minecraft.world.ItemInteractionResult;
//?}

public class FoldSocketBlock extends Block {
    public static final BooleanProperty FILLED = BooleanProperty.create("filled");
    public static final int RANGE = 48;
    public static final int REQUIRED = 4;

    public FoldSocketBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FILLED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FILLED);
    }

    //? if >=26.1 {
    /*@Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        return UseResults.item(insertShard(stack, state, level, pos, player), level);
    }
    *///?} else {
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                              InteractionHand hand, BlockHitResult hit) {
        return UseResults.item(insertShard(stack, state, level, pos, player), level);
    }
    //?}

    private UseResults.Outcome insertShard(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player) {
        if (state.getValue(FILLED) || !stack.is(ReliquaryItems.FOLD_SHARD.get())) {
            return UseResults.Outcome.PASS_TO_DEFAULT;
        }
        if (level instanceof ServerLevel server) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            server.setBlock(pos, state.setValue(FILLED, true), Block.UPDATE_ALL);
            server.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.2F, 0.9F);
            server.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 16, 0.4, 0.4, 0.4, 0.05);
            List<BlockPos> sockets = scan(server, pos, s -> s.is(ReliquaryBlocks.FOLD_SOCKET.get()));
            int empty = 0;
            for (BlockPos socket : sockets) {
                if (!server.getBlockState(socket).getValue(FILLED)) {
                    empty++;
                }
            }
            int missing = empty + Math.max(0, REQUIRED - sockets.size());
            if (missing == 0) {
                McPlayers.overlay(player, Component.translatable("message.upfall.fold_socket.complete"));
                dissolveSeals(server, pos);
                FoldWarden.summon(server, sockets);
            } else {
                McPlayers.overlay(player, Component.translatable("message.upfall.fold_socket.remaining", missing));
            }
        }
        return UseResults.Outcome.SIDED_SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide()) {
            McPlayers.overlay(player, Component.translatable(state.getValue(FILLED) ? "message.upfall.fold_socket.filled" : "message.upfall.fold_socket.empty"));
        }
        return UseResults.sided(level);
    }

    private static void dissolveSeals(ServerLevel level, BlockPos origin) {
        List<BlockPos> seals = scan(level, origin, s -> s.is(ReliquaryBlocks.FOLD_SEAL.get()));
        Block seal = ReliquaryBlocks.FOLD_SEAL.get();
        level.playSound(null, origin, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 2.0F, 0.6F);
        markFloor(level, seals);
        for (BlockPos pos : seals) {
            int delay = 4 + (int) Math.min(80, Math.sqrt(pos.distSqr(origin)) * 1.5);
            level.scheduleTick(pos, seal, delay);
        }
    }

    private static void markFloor(ServerLevel level, List<BlockPos> seals) {
        if (seals.isEmpty()) return;
        int floorY = seals.stream().mapToInt(BlockPos::getY).min().orElseThrow();
        List<BlockPos> floor = seals.stream().filter(pos -> pos.getY() == floorY).toList();
        int minX = floor.stream().mapToInt(BlockPos::getX).min().orElseThrow();
        int maxX = floor.stream().mapToInt(BlockPos::getX).max().orElseThrow();
        int minZ = floor.stream().mapToInt(BlockPos::getZ).min().orElseThrow();
        int maxZ = floor.stream().mapToInt(BlockPos::getZ).max().orElseThrow();
        for (BlockPos pos : floor) {
            boolean edge = pos.getX() == minX || pos.getX() == maxX || pos.getZ() == minZ || pos.getZ() == maxZ;
            BlockState state = level.getBlockState(pos);
            level.setBlock(pos, state.setValue(FoldSealBlock.FLOOR_KIND, edge ? FoldSealBlock.FLOOR_EDGE : FoldSealBlock.FLOOR), Block.UPDATE_CLIENTS);
        }
    }

    public static List<BlockPos> scan(ServerLevel level, BlockPos origin, Predicate<BlockState> predicate) {
        List<BlockPos> found = new ArrayList<>();
        int minX = origin.getX() - RANGE;
        int maxX = origin.getX() + RANGE;
        int minY = origin.getY() - RANGE;
        int maxY = origin.getY() + RANGE;
        int minZ = origin.getZ() - RANGE;
        int maxZ = origin.getZ() + RANGE;
        for (int cx = minX >> 4; cx <= maxX >> 4; cx++) {
            for (int cz = minZ >> 4; cz <= maxZ >> 4; cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) {
                    continue;
                }
                LevelChunkSection[] sections = chunk.getSections();
                for (int i = 0; i < sections.length; i++) {
                    LevelChunkSection section = sections[i];
                    int baseY = chunk.getSectionYFromSectionIndex(i) << 4;
                    if (baseY + 15 < minY || baseY > maxY || section.hasOnlyAir() || !section.maybeHas(predicate)) {
                        continue;
                    }
                    for (int y = 0; y < 16; y++) {
                        int wy = baseY + y;
                        if (wy < minY || wy > maxY) {
                            continue;
                        }
                        for (int z = 0; z < 16; z++) {
                            int wz = (cz << 4) + z;
                            if (wz < minZ || wz > maxZ) {
                                continue;
                            }
                            for (int x = 0; x < 16; x++) {
                                int wx = (cx << 4) + x;
                                if (wx >= minX && wx <= maxX && predicate.test(section.getBlockState(x, y, z))) {
                                    found.add(new BlockPos(wx, wy, wz));
                                }
                            }
                        }
                    }
                }
            }
        }
        return found;
    }
}
