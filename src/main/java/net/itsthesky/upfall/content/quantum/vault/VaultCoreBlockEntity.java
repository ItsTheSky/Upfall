package net.itsthesky.upfall.content.quantum.vault;

import net.itsthesky.upfall.UpfallConfig;
import net.itsthesky.upfall.compat.mc.DataIn;
import net.itsthesky.upfall.compat.mc.DataOut;
import net.itsthesky.upfall.compat.mc.UpfallBlockEntity;
import net.itsthesky.upfall.content.quantum.QuantumBlocks;
import net.itsthesky.upfall.content.quantum.QuantumParticles;
import net.itsthesky.upfall.content.quantum.QuantumSounds;
import net.itsthesky.upfall.content.quantum.observe.QuantumObservation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class VaultCoreBlockEntity extends UpfallBlockEntity {
    private static final int JUMP_COOLDOWN = 30;
    private static final int DARK_REARM = 100;
    private static final double FROST_RANGE = 2.2;

    private final List<BlockPos> chestSpots = new ArrayList<>();
    private final List<Direction> chestFacings = new ArrayList<>();
    private final List<BlockPos> frostPanes = new ArrayList<>();
    private final List<BlockPos> lamps = new ArrayList<>();
    private final List<BoundingBox> darkZones = new ArrayList<>();
    private BoundingBox interior = new BoundingBox(0, 0, 0, 0, 0, 0);
    private int chestState;
    private long lastJump;
    private long lightsOutUntil = -1;
    private long darkRearm;

    public VaultCoreBlockEntity(BlockPos pos, BlockState state) {
        super(QuantumBlocks.VAULT_CORE_ENTITY.get(), pos, state);
    }

    public void configure(List<BlockPos> chests, List<Direction> facings, int state, List<BlockPos> frost, List<BlockPos> lampList,
                          List<BoundingBox> zones, BoundingBox inside) {
        chestSpots.clear();
        chestSpots.addAll(chests);
        chestFacings.clear();
        chestFacings.addAll(facings);
        chestState = state;
        frostPanes.clear();
        frostPanes.addAll(frost);
        lamps.clear();
        lamps.addAll(lampList);
        darkZones.clear();
        darkZones.addAll(zones);
        interior = inside;
        setChanged();
    }

    public int chestState() {
        return chestState;
    }

    public List<BlockPos> chestSpots() {
        return chestSpots;
    }

    public boolean isDark() {
        return lightsOutUntil >= 0;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, VaultCoreBlockEntity core) {
        if (!(level instanceof ServerLevel server) || core.chestSpots.isEmpty() || !QuantumObservation.shouldCheck(level, pos)) return;
        long now = level.getGameTime();
        List<ServerPlayer> inside = new ArrayList<>();
        for (ServerPlayer player : server.players()) {
            if (QuantumObservation.canObserve(player) && core.interior.inflatedBy(1).isInside(player.blockPosition())) inside.add(player);
        }
        core.tickChest(server, now);
        core.tickLights(server, inside, now);
        if (!inside.isEmpty()) {
            core.tickFrost(server, inside);
            core.tickLures(server, inside);
        }
    }


    private void tickChest(ServerLevel level, long now) {
        BlockPos current = chestSpots.get(chestState);
        if (!(level.getBlockEntity(current) instanceof ShiftingChestBlockEntity chest)) return;
        if (now - lastJump < JUMP_COOLDOWN || chest.isViewed()) return;
        if (!QuantumObservation.anyPlayerNear(level, current.getCenter())) return;
        if (QuantumObservation.isObserved(level, current)) return;
        List<Integer> targets = new ArrayList<>();
        for (int i = 0; i < chestSpots.size(); i++) {
            BlockPos spot = chestSpots.get(i);
            if (i != chestState && level.getBlockState(spot).canBeReplaced() && !QuantumObservation.isObserved(level, spot)) targets.add(i);
        }
        if (targets.isEmpty()) return;
        moveChest(level, targets.get(level.getRandom().nextInt(targets.size())));
        lastJump = now;
    }

    public void moveChest(ServerLevel level, int target) {
        BlockPos from = chestSpots.get(chestState);
        BlockPos to = chestSpots.get(target);
        if (!(level.getBlockEntity(from) instanceof ShiftingChestBlockEntity chest)) return;
        CompoundTag contents = chest.takeContents(level.registryAccess());
        level.setBlock(from, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        Direction facing = target < chestFacings.size() ? chestFacings.get(target) : Direction.NORTH;
        level.setBlock(to, QuantumBlocks.SHIFTING_CHEST.get().defaultBlockState().setValue(ShiftingChestBlock.FACING, facing), Block.UPDATE_ALL);
        if (level.getBlockEntity(to) instanceof ShiftingChestBlockEntity moved) {
            moved.receiveContents(contents, level.registryAccess());
        }
        level.playSound(null, from, QuantumSounds.CHEST_SHIFT.get(), SoundSource.BLOCKS, 0.7F, 1.0F);
        level.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), from.getX() + 0.5, from.getY() + 0.5, from.getZ() + 0.5, 16, 0.4, 0.4, 0.4, 0.02);
        chestState = target;
        setChanged();
    }


    private void tickFrost(ServerLevel level, List<ServerPlayer> players) {
        int ticks = UpfallConfig.getInt(UpfallConfig.VAULT_FROST_TICKS);
        for (BlockPos pane : frostPanes) {
            BlockState state = level.getBlockState(pane);
            if (!(state.getBlock() instanceof FrostGlassBlock)) continue;
            Vec3 center = pane.getCenter();
            for (ServerPlayer player : players) {
                if (player.position().add(0.0, 0.9, 0.0).distanceTo(center) <= FROST_RANGE) {
                    FrostGlassBlock.frost(level, pane, state, ticks);
                    break;
                }
            }
        }
    }

    private void tickLights(ServerLevel level, List<ServerPlayer> players, long now) {
        if (lightsOutUntil >= 0) {
            if (now >= lightsOutUntil) {
                setLamps(level, true);
                lightsOutUntil = -1;
                darkRearm = now + DARK_REARM;
                setChanged();
            }
            return;
        }
        if (now < darkRearm) return;
        for (ServerPlayer player : players) {
            for (BoundingBox zone : darkZones) {
                if (zone.isInside(player.blockPosition())) {
                    setLamps(level, false);
                    lightsOutUntil = now + UpfallConfig.getInt(UpfallConfig.VAULT_LIGHTS_OUT_TICKS);
                    level.playSound(null, player.blockPosition(), QuantumSounds.LIGHTS_OUT.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
                    setChanged();
                    return;
                }
            }
        }
    }

    private void setLamps(ServerLevel level, boolean lit) {
        for (BlockPos lamp : lamps) {
            BlockState state = level.getBlockState(lamp);
            if (state.getBlock() instanceof VaultLampBlock && state.getValue(VaultLampBlock.LIT) != lit) {
                level.setBlock(lamp, state.setValue(VaultLampBlock.LIT, lit), Block.UPDATE_ALL);
            }
        }
    }

    private void tickLures(ServerLevel level, List<ServerPlayer> players) {
        double chance = UpfallConfig.getInt(UpfallConfig.OBSERVATION_INTERVAL) / (double) UpfallConfig.getInt(UpfallConfig.VAULT_WHISPER_INTERVAL);
        for (ServerPlayer player : players) {
            if (level.getRandom().nextDouble() >= chance) continue;
            Vec3 look = player.getViewVector(1.0F);
            Vec3 behind = player.getEyePosition().subtract(new Vec3(look.x, 0.0, look.z).normalize().scale(2.5 + level.getRandom().nextDouble() * 2.0));
            player.connection.send(new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(QuantumSounds.WHISPER.get()),
                    SoundSource.AMBIENT, behind.x, behind.y, behind.z, 0.9F, 0.8F + level.getRandom().nextFloat() * 0.4F, level.getRandom().nextLong()));
        }
    }


    @Override
    protected void saveData(DataOut out) {
        out.putLongArray("Chests", chestSpots.stream().mapToLong(BlockPos::asLong).toArray());
        out.putIntArray("Facings", chestFacings.stream().mapToInt(Direction::get3DDataValue).toArray());
        out.putInt("State", chestState);
        out.putLongArray("Frost", frostPanes.stream().mapToLong(BlockPos::asLong).toArray());
        out.putLongArray("Lamps", lamps.stream().mapToLong(BlockPos::asLong).toArray());
        int[] zones = new int[darkZones.size() * 6];
        for (int i = 0; i < darkZones.size(); i++) System.arraycopy(box(darkZones.get(i)), 0, zones, i * 6, 6);
        out.putIntArray("Zones", zones);
        out.putIntArray("Interior", box(interior));
        out.putLong("LightsOut", lightsOutUntil);
    }

    @Override
    protected void loadData(DataIn in) {
        chestSpots.clear();
        for (long l : in.getLongArrayOrEmpty("Chests")) chestSpots.add(BlockPos.of(l));
        chestFacings.clear();
        for (int f : in.getIntArrayOrEmpty("Facings")) chestFacings.add(Direction.from3DDataValue(f));
        chestState = in.getIntOr("State", 0);
        frostPanes.clear();
        for (long l : in.getLongArrayOrEmpty("Frost")) frostPanes.add(BlockPos.of(l));
        lamps.clear();
        for (long l : in.getLongArrayOrEmpty("Lamps")) lamps.add(BlockPos.of(l));
        darkZones.clear();
        int[] zones = in.getIntArrayOrEmpty("Zones");
        for (int i = 0; i + 5 < zones.length; i += 6) darkZones.add(unbox(zones, i));
        int[] bounds = in.getIntArrayOrEmpty("Interior");
        if (bounds.length == 6) interior = unbox(bounds, 0);
        lightsOutUntil = in.getLongOr("LightsOut", -1L);
    }

    private static int[] box(BoundingBox box) {
        return new int[]{box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ()};
    }

    private static BoundingBox unbox(int[] a, int i) {
        return new BoundingBox(a[i], a[i + 1], a[i + 2], a[i + 3], a[i + 4], a[i + 5]);
    }
}
