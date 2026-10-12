package net.itsthesky.upfall.content.quantum.pedestal;

import net.itsthesky.upfall.UpfallConfig;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.QuantumParticles;
import net.itsthesky.upfall.content.quantum.QuantumRecipeTypes;
import net.itsthesky.upfall.content.quantum.QuantumSounds;
import net.itsthesky.upfall.content.quantum.QuantumTriggers;
import net.itsthesky.upfall.content.quantum.observe.QuantumObservation;
import net.itsthesky.upfall.content.quantum.upgrade.QuantumUpgrades;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class QuantumPedestalBlockEntity extends PedestalBlockEntity {
    private @Nullable UUID ritualist;
    private int age;
    private int duration;
    private boolean active;

    public QuantumPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(QuantumBlocks.QUANTUM_PEDESTAL_ENTITY.get(), pos, state);
    }

    public boolean active() {
        return active;
    }

    public static List<BlockPos> sides(BlockPos center) {
        int d = UpfallConfig.getInt(UpfallConfig.PEDESTAL_RING_DISTANCE);
        return List.of(center.offset(d, 0, 0), center.offset(-d, 0, 0), center.offset(0, 0, d), center.offset(0, 0, -d));
    }

    public static List<BlockPos> corners(BlockPos center) {
        int d = UpfallConfig.getInt(UpfallConfig.PEDESTAL_RING_DISTANCE);
        return List.of(center.offset(d, 0, d), center.offset(-d, 0, d), center.offset(d, 0, -d), center.offset(-d, 0, -d));
    }

    private @Nullable List<PedestalBlockEntity> ring(List<BlockPos> positions) {
        List<PedestalBlockEntity> result = new ArrayList<>();
        for (BlockPos pos : positions) {
            if (!(level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal) || pedestal instanceof QuantumPedestalBlockEntity) return null;
            result.add(pedestal);
        }
        return result;
    }

    private @Nullable IncantationInput input() {
        List<PedestalBlockEntity> sides = ring(sides(worldPosition));
        List<PedestalBlockEntity> corners = ring(corners(worldPosition));
        if (sides == null || corners == null) return null;
        return new IncantationInput(item(), sides.stream().map(PedestalBlockEntity::item).toList(), corners.stream().map(PedestalBlockEntity::item).toList());
    }

    private Optional<RecipeHolder<QuantumIncantationRecipe>> recipe(IncantationInput input) {
        return level.getRecipeManager().getRecipeFor(QuantumRecipeTypes.INCANTATION.get(), input, level);
    }

    public @Nullable Component tryStart(ServerPlayer player) {
        if (active) return Component.translatable("message.upfall.incantation.busy");
        if (item().isEmpty()) return Component.translatable("message.upfall.incantation.no_piece");
        IncantationInput input = input();
        if (input == null) {
            return Component.translatable("message.upfall.incantation.incomplete", UpfallConfig.getInt(UpfallConfig.PEDESTAL_RING_DISTANCE));
        }
        Optional<RecipeHolder<QuantumIncantationRecipe>> recipe = recipe(input);
        if (recipe.isEmpty()) return Component.translatable("message.upfall.incantation.no_match");
        QuantumUpgrades.Refusal refusal = QuantumUpgrades.canApply(item(), recipe.get().value().upgrade());
        switch (refusal) {
            case NOT_APPLICABLE -> {
                return Component.translatable("message.upfall.incantation.not_applicable");
            }
            case DUPLICATE -> {
                return Component.translatable("message.upfall.incantation.duplicate");
            }
            case FULL -> {
                return Component.translatable("message.upfall.incantation.full", UpfallConfig.getInt(UpfallConfig.MAX_UPGRADES));
            }
            default -> {}
        }
        ritualist = player.getUUID();
        age = 0;
        duration = UpfallConfig.getInt(UpfallConfig.RITUAL_TICKS);
        active = true;
        sync();
        level.playSound(null, worldPosition, QuantumSounds.RITUAL_START.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        return null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, QuantumPedestalBlockEntity entity) {
        if (entity.active && level instanceof ServerLevel server) entity.tickRitual(server);
    }

    private void tickRitual(ServerLevel server) {
        Player player = ritualist == null ? null : server.getPlayerByUUID(ritualist);
        if (player == null || item().isEmpty() || !QuantumObservation.isObservedBy(player, worldPosition)) {
            fail(server);
            return;
        }
        age++;
        Vec3 top = Vec3.atCenterOf(worldPosition).add(0.0, 0.9, 0.0);
        if (age % 2 == 0) {
            List<BlockPos> ring = new ArrayList<>(sides(worldPosition));
            ring.addAll(corners(worldPosition));
            for (BlockPos from : ring) {
                Vec3 start = Vec3.atCenterOf(from).add(0.0, 0.9, 0.0);
                Vec3 velocity = top.subtract(start).scale(0.14);
                server.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), start.x, start.y, start.z, 0, velocity.x, velocity.y, velocity.z, 1.0);
            }
            server.sendParticles(QuantumParticles.VEIL_MOTE.get(), top.x, top.y, top.z, 2, 0.3, 0.3, 0.3, 0.01);
        }
        if (age % 20 == 1) {
            float progress = (float) age / Math.max(1, duration);
            server.playSound(null, worldPosition, QuantumSounds.RITUAL_HUM.get(), SoundSource.BLOCKS, 0.8F, 0.8F + progress * 0.6F);
        }
        if (age >= duration) complete(server, player);
    }

    private void complete(ServerLevel server, Player player) {
        IncantationInput input = input();
        Optional<RecipeHolder<QuantumIncantationRecipe>> recipe = input == null ? Optional.empty() : recipe(input);
        if (recipe.isEmpty() || QuantumUpgrades.canApply(item(), recipe.get().value().upgrade()) != QuantumUpgrades.Refusal.NONE) {
            fail(server);
            return;
        }
        ItemStack result = recipe.get().value().assemble(input, server.registryAccess());
        consumeRing();
        stop();
        setItem(result);
        Vec3 top = Vec3.atCenterOf(worldPosition).add(0.0, 0.9, 0.0);
        server.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), top.x, top.y, top.z, 40, 0.4, 0.4, 0.4, 0.15);
        server.sendParticles(ParticleTypes.END_ROD, top.x, top.y, top.z, 12, 0.2, 0.2, 0.2, 0.05);
        server.playSound(null, worldPosition, QuantumSounds.RITUAL_SUCCESS.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        if (player instanceof ServerPlayer serverPlayer) QuantumTriggers.INCANTATION.get().trigger(serverPlayer);
    }

    private void fail(ServerLevel server) {
        consumeRing();
        stop();
        sync();
        Vec3 top = Vec3.atCenterOf(worldPosition).add(0.0, 0.9, 0.0);
        server.sendParticles(ParticleTypes.LARGE_SMOKE, top.x, top.y, top.z, 12, 0.3, 0.2, 0.3, 0.02);
        server.playSound(null, worldPosition, QuantumSounds.RITUAL_FAIL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        Player player = ritualist == null ? null : server.getPlayerByUUID(ritualist);
        if (player != null) player.displayClientMessage(Component.translatable("message.upfall.incantation.collapsed"), true);
    }

    private void consumeRing() {
        List<BlockPos> ring = new ArrayList<>(sides(worldPosition));
        ring.addAll(corners(worldPosition));
        for (BlockPos pos : ring) {
            if (level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestal && !(pedestal instanceof QuantumPedestalBlockEntity)
                    && !pedestal.item().isEmpty()) {
                pedestal.takeItem();
                if (level instanceof ServerLevel server) {
                    server.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, 6, 0.15, 0.15, 0.15, 0.03);
                }
            }
        }
    }

    private void stop() {
        active = false;
        age = 0;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putBoolean("Active", active);
        return tag;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        active = tag.getBoolean("Active");
    }
}
