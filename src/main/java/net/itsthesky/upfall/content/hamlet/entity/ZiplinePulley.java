package net.itsthesky.upfall.content.hamlet.entity;

import net.itsthesky.upfall.compat.mc.DataIn;
import net.itsthesky.upfall.compat.mc.DataOut;
import net.itsthesky.upfall.compat.mc.McEntities;
import net.itsthesky.upfall.content.hamlet.HamletEntities;
import net.itsthesky.upfall.content.hamlet.HamletSounds;
import net.itsthesky.upfall.content.hamlet.block.RopeAnchorBlockEntity;
import net.itsthesky.upfall.content.hamlet.block.ZiplineAnchorBlock;
import net.itsthesky.upfall.content.hamlet.rope.RopeLines;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class ZiplinePulley extends Entity {
    private static final EntityDataAccessor<BlockPos> DATA_FROM = SynchedEntityData.defineId(ZiplinePulley.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<BlockPos> DATA_TO = SynchedEntityData.defineId(ZiplinePulley.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Float> DATA_PROGRESS = SynchedEntityData.defineId(ZiplinePulley.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_SPEED = SynchedEntityData.defineId(ZiplinePulley.class, EntityDataSerializers.FLOAT);

    public static final double MIN_SPEED = 0.15;
    public static final double MAX_SPEED = 0.9;
    private static final double GRAVITY = 0.05;
    private static final double DRAG = 0.003;
    public static final double END_MARGIN = 1.3;
    private static final double GRIP = 0.3;
    private static final double MAX_REACH = 4.5;
    private static final double LATCH_DISTANCE = 2.0;

    public enum Result {
        STARTED, NO_LINE, TOO_FAR, BLOCKED, BUSY;

        public String message() {
            return "message.upfall.zipline." + name().toLowerCase(Locale.ROOT);
        }
    }

    private Vec3 from = Vec3.ZERO;
    private Vec3 to = Vec3.ZERO;
    private double sag;
    private double length;
    private double margin = END_MARGIN;
    private boolean lineReady;
    private boolean synced;
    private double progress;
    private double speed = MIN_SPEED;
    @Nullable
    private Vec3 landing;

    public ZiplinePulley(EntityType<? extends ZiplinePulley> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public static Result rideFromAnchor(Entity rider, BlockPos anchor) {
        BlockPos other = RopeLines.ziplinePartner(rider.level(), anchor);
        if (other == null) {
            return Result.NO_LINE;
        }
        if (rider.getEyePosition().distanceTo(attach(anchor)) > MAX_REACH) {
            return Result.TOO_FAR;
        }
        return start(rider, anchor, other, 0.0);
    }

    public static Result latchNearest(Entity rider) {
        Level level = rider.level();
        Vec3 hands = rider.getEyePosition().add(0.0, 0.35, 0.0);
        BlockPos bestFrom = null;
        BlockPos bestTo = null;
        double bestStart = 0.0;
        double bestDistance = LATCH_DISTANCE;
        int cx = rider.blockPosition().getX() >> 4;
        int cz = rider.blockPosition().getZ() >> 4;
        for (int x = cx - 3; x <= cx + 3; x++) {
            for (int z = cz - 3; z <= cz + 3; z++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(x, z);
                if (chunk == null) continue;
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (!(be instanceof RopeAnchorBlockEntity) || !(be.getBlockState().getBlock() instanceof ZiplineAnchorBlock)) continue;
                    BlockPos a = be.getBlockPos();
                    BlockPos b = RopeLines.ziplinePartner(level, a);
                    if (b == null || a.asLong() > b.asLong()) continue;
                    Vec3 pa = attach(a);
                    Vec3 pb = attach(b);
                    double lineSag = RopeLines.sag(pa, pb, true);
                    double t = RopeLines.closestT(pa, pb, lineSag, hands);
                    double distance = RopeLines.point(pa, pb, lineSag, t).distanceTo(hands);
                    if (distance >= bestDistance) continue;
                    double length = pa.distanceTo(pb);
                    Vec3 along = pb.subtract(pa);
                    boolean forward = rider.getLookAngle().x * along.x + rider.getLookAngle().z * along.z >= 0.0;
                    bestDistance = distance;
                    bestFrom = forward ? a : b;
                    bestTo = forward ? b : a;
                    bestStart = forward ? t * length : (1.0 - t) * length;
                }
            }
        }
        if (bestFrom == null) {
            return Result.NO_LINE;
        }
        double length = attach(bestFrom).distanceTo(attach(bestTo));
        if (bestStart > length - endMargin(attach(bestFrom), attach(bestTo)) - 0.5) {
            return Result.BLOCKED;
        }
        return start(rider, bestFrom, bestTo, bestStart);
    }

    public static Result start(Entity rider, BlockPos fromAnchor, BlockPos toAnchor, double startDistance) {
        if (rider.isPassenger() || rider.isVehicle()) {
            return Result.BUSY;
        }
        Level level = rider.level();
        ZiplinePulley pulley = new ZiplinePulley(HamletEntities.ZIPLINE_PULLEY.get(), level);
        pulley.setLine(fromAnchor, toAnchor);
        pulley.progress = Mth.clamp(startDistance, pulley.margin, Math.max(pulley.margin, pulley.length - pulley.margin));
        pulley.entityData.set(DATA_PROGRESS, (float) pulley.progress);
        pulley.setPos(pulley.linePoint(pulley.progress));
        pulley.faceAlongLine();
        if (pulley.length < pulley.margin * 2.0 + 0.5 || !pulley.isClear(rider, pulley.progress)) {
            return Result.BLOCKED;
        }
        level.addFreshEntity(pulley);
        //? if >=26.1 {
        /*if (!rider.startRiding(pulley, true, true)) {
        *///?} else {
        if (!rider.startRiding(pulley, true)) {
        //?}
            pulley.discard();
            return Result.BUSY;
        }
        pulley.positionRider(rider);
        rider.resetFallDistance();
        level.playSound(null, pulley.getX(), pulley.getY(), pulley.getZ(), HamletSounds.ZIPLINE_ATTACH.get(), SoundSource.PLAYERS, 0.8F, 1.0F);
        return Result.STARTED;
    }

    public static double endMargin(Vec3 from, Vec3 to) {
        double horizontal = Math.sqrt((to.x - from.x) * (to.x - from.x) + (to.z - from.z) * (to.z - from.z));
        return END_MARGIN * from.distanceTo(to) / Math.max(0.5, horizontal);
    }

    public static Vec3 attach(BlockPos anchor) {
        return new Vec3(anchor.getX() + 0.5, anchor.getY() + 0.8125, anchor.getZ() + 0.5);
    }

    private void setLine(BlockPos fromAnchor, BlockPos toAnchor) {
        entityData.set(DATA_FROM, fromAnchor);
        entityData.set(DATA_TO, toAnchor);
        updateLine();
    }

    private void updateLine() {
        from = attach(entityData.get(DATA_FROM));
        to = attach(entityData.get(DATA_TO));
        sag = RopeLines.sag(from, to, true);
        length = Math.max(1.0E-3, from.distanceTo(to));
        margin = endMargin(from, to);
        lineReady = true;
    }

    private Vec3 linePoint(double distance) {
        return RopeLines.point(from, to, sag, Mth.clamp(distance / length, 0.0, 1.0));
    }

    private double descent(double distance) {
        double t = Mth.clamp(distance / length, 0.0, 1.0);
        double dy = (to.y - from.y) - sag * 4.0 * (1.0 - 2.0 * t);
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        return -dy / Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private void faceAlongLine() {
        setYRot((float) (Mth.atan2(to.z - from.z, to.x - from.x) * Mth.RAD_TO_DEG) - 90.0F);
    }

    private AABB riderBox(Entity rider, double distance) {
        Vec3 grip = linePoint(distance);
        double halfWidth = rider.getBbWidth() / 2.0;
        double feet = grip.y - rider.getBbHeight() - GRIP;
        return new AABB(grip.x - halfWidth, feet, grip.z - halfWidth, grip.x + halfWidth, grip.y, grip.z + halfWidth).deflate(0.01);
    }

    private boolean isClear(Entity rider, double distance) {
        return level().noCollision(rider, riderBox(rider, distance));
    }

    public double getSpeed() {
        return speed;
    }

    public double getProgress() {
        return progress;
    }

    public double getLength() {
        return length;
    }

    @Override
    public void tick() {
        super.tick();
        if (!lineReady) {
            updateLine();
        }
        Entity rider = getFirstPassenger();
        boolean server = !level().isClientSide();
        if (server && (rider == null || !anchorsIntact())) {
            if (rider != null) {
                letGo(rider, null);
            }
            discard();
            return;
        }
        speed = Mth.clamp(speed + GRAVITY * descent(progress) - DRAG, MIN_SPEED, MAX_SPEED);
        double next = progress + speed;
        if (next >= length - margin) {
            next = length - margin;
            if (server) {
                setPos(linePoint(next));
                letGo(rider, findLanding(rider));
                return;
            }
        }
        if (server && !isClear(rider, next)) {
            letGo(rider, length - next < margin + 3.0 ? findLanding(rider) : null);
            return;
        }
        progress = next;
        setPos(linePoint(progress));
        faceAlongLine();
        if (rider != null) {
            rider.resetFallDistance();
        }
        if (server && tickCount % 4 == 0) {
            entityData.set(DATA_PROGRESS, (float) progress);
            entityData.set(DATA_SPEED, (float) speed);
        }
    }

    private boolean anchorsIntact() {
        BlockPos a = entityData.get(DATA_FROM);
        BlockPos b = entityData.get(DATA_TO);
        if (!level().isLoaded(a) || !level().isLoaded(b)) {
            return true;
        }
        BlockState state = level().getBlockState(a);
        return state.getBlock() instanceof ZiplineAnchorBlock && level().getBlockState(b).getBlock() instanceof ZiplineAnchorBlock
                && level().getBlockEntity(a) instanceof RopeAnchorBlockEntity be && be.isLinkedTo(b);
    }

    private void letGo(Entity rider, @Nullable Vec3 landing) {
        this.landing = landing;
        rider.stopRiding();
        if (landing != null && !(rider instanceof LivingEntity)) {
            McEntities.snapTo(rider, landing.x, landing.y, landing.z, rider.getYRot(), rider.getXRot());
        }
        if (rider instanceof ServerPlayer player) {
            player.connection.teleport(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        }
        rider.resetFallDistance();
        level().playSound(null, getX(), getY(), getZ(), HamletSounds.ZIPLINE_DETACH.get(), SoundSource.PLAYERS, 0.7F, 1.0F);
        discard();
    }

    @Nullable
    private Vec3 findLanding(Entity rider) {
        BlockPos post = entityData.get(DATA_TO);
        AABB hanging = riderBox(rider, length - margin);
        Vec3 hangingFeet = new Vec3(hanging.getCenter().x, hanging.minY, hanging.getCenter().z);
        Vec3 best = null;
        double bestDistance = Double.MAX_VALUE;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dy = 0; dy >= -6; dy--) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dz == 0) continue;
                    pos.set(post.getX() + dx, post.getY() + dy, post.getZ() + dz);
                    BlockPos below = pos.below();
                    VoxelShape floor = level().getBlockState(below).getCollisionShape(level(), below);
                    if (floor.isEmpty()) continue;
                    double top = below.getY() + floor.max(Direction.Axis.Y);
                    if (top > pos.getY() + 0.01 || top <= below.getY() + 0.05) continue;
                    Vec3 feet = new Vec3(pos.getX() + 0.5, top, pos.getZ() + 0.5);
                    AABB box = rider.getDimensions(rider.getPose()).makeBoundingBox(feet);
                    if (!level().noCollision(rider, box)) continue;
                    double distance = feet.distanceToSqr(hangingFeet);
                    if (distance < bestDistance) {
                        bestDistance = distance;
                        best = feet;
                    }
                }
            }
        }
        return best;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        return landing != null ? landing : passenger.position();
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        if (hasPassenger(passenger)) {
            callback.accept(passenger, getX(), getY() - passenger.getBbHeight() - GRIP, getZ());
        }
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return getPassengers().isEmpty();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_FROM, BlockPos.ZERO);
        builder.define(DATA_TO, BlockPos.ZERO);
        builder.define(DATA_PROGRESS, 0.0F);
        builder.define(DATA_SPEED, (float) MIN_SPEED);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_FROM.equals(key) || DATA_TO.equals(key)) {
            lineReady = false;
        } else if (level().isClientSide() && DATA_PROGRESS.equals(key)) {
            double serverProgress = entityData.get(DATA_PROGRESS);
            if (!synced || Math.abs(serverProgress - progress) > 0.8) {
                progress = serverProgress;
                speed = entityData.get(DATA_SPEED);
                synced = true;
            }
        }
    }

    // 26.1.2 has no lerpTo: the no-op handler keeps the client pulley driven by its own simulation, as the empty lerpTo did.
    // TODO: check in game that server position packets do not make the client pulley jitter.
    //? if >=26.1 {
    /*private final net.minecraft.world.entity.InterpolationHandler noInterpolation = new net.minecraft.world.entity.InterpolationHandler(this) {
        @Override
        public void interpolateTo(Vec3 position, float yRot, float xRot) {
        }
    };

    @Override
    public net.minecraft.world.entity.InterpolationHandler getInterpolation() {
        return noInterpolation;
    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel level, net.minecraft.world.damagesource.DamageSource source, float damage) {
        if (!isInvulnerableToBase(source)) {
            markHurt();
        }
        return false;
    }

    @Override
    protected void readAdditionalSaveData(net.minecraft.world.level.storage.ValueInput input) {
        load(DataIn.of(input));
    }

    @Override
    protected void addAdditionalSaveData(net.minecraft.world.level.storage.ValueOutput output) {
        save(DataOut.of(output));
    }
    *///?} else {
    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        load(DataIn.of(tag, registryAccess()));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        save(DataOut.of(tag, registryAccess()));
    }
    //?}

    private void load(DataIn in) {
        entityData.set(DATA_FROM, BlockPos.of(in.getLongOr("From", 0L)));
        entityData.set(DATA_TO, BlockPos.of(in.getLongOr("To", 0L)));
        updateLine();
        progress = in.getDoubleOr("Progress", 0.0);
        speed = Math.max(MIN_SPEED, in.getDoubleOr("Speed", 0.0));
        entityData.set(DATA_PROGRESS, (float) progress);
    }

    private void save(DataOut out) {
        out.putLong("From", entityData.get(DATA_FROM).asLong());
        out.putLong("To", entityData.get(DATA_TO).asLong());
        out.putDouble("Progress", progress);
        out.putDouble("Speed", speed);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean skipAttackInteraction(Entity entity) {
        return true;
    }
}
