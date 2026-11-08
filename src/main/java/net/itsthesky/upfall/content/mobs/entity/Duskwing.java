package net.itsthesky.upfall.content.mobs.entity;

import net.itsthesky.upfall.compat.mc.UpfallFlyingMob;
import net.itsthesky.upfall.content.mobs.MobSounds;
import net.itsthesky.upfall.registry.ModTags;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class Duskwing extends UpfallFlyingMob implements Enemy {
    private static final EntityDataAccessor<Boolean> DIVING = SynchedEntityData.defineId(Duskwing.class, EntityDataSerializers.BOOLEAN);
    private static final int MIN_Y = 70;
    private static final int MAX_Y = 240;
    private static final int SPAWN_RARITY = 8;
    private static final int MAX_NEARBY = 2;
    private static final double CROWD_RANGE = 128.0;
    private static final List<EquipmentSlot> ARMOR_SLOTS = List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);

    public Duskwing(EntityType<? extends Duskwing> type, Level level) {
        super(type, level);
        this.moveControl = new AirSteering.SteerMoveControl(this, 0.1, 0.55);
        this.xpReward = 10;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 26.0)
                .add(Attributes.ARMOR, 4.0)
                .add(Attributes.ATTACK_DAMAGE, 6.75)
                .add(Attributes.ATTACK_KNOCKBACK, 0.8)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.FLYING_SPEED, 0.9);
    }

    public static boolean checkSpawnRules(EntityType<Duskwing> type, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && ModDimensions.isUpfall(level.getLevel())
                && pos.getY() > MIN_Y && pos.getY() < MAX_Y
                && level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir() && level.getBlockState(pos.below()).isAir()
                && (spawnType != MobSpawnType.NATURAL || rareAndUncrowded(level, pos, random));
    }

    // Open sky passes the placement checks almost everywhere, so without this the monster cap fills with duskwings
    private static boolean rareAndUncrowded(ServerLevelAccessor level, BlockPos pos, RandomSource random) {
        return random.nextInt(SPAWN_RARITY) == 0
                && level.getEntitiesOfClass(Duskwing.class, AABB.ofSize(Vec3.atCenterOf(pos), CROWD_RANGE, CROWD_RANGE, CROWD_RANGE)).size() < MAX_NEARBY;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DIVING, false);
    }

    public boolean isDiving() {
        return entityData.get(DIVING);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new DiveAttackGoal());
        this.goalSelector.addGoal(2, new AirSteering.DriftGoal(this, MIN_Y, MAX_Y, 28, 8, 0.8, 160));
        //? if >=26.1 {
        /*this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, (target, level) -> wearsSkyMetal(target)));
        *///?} else {
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true, Duskwing::wearsSkyMetal));
        //?}
    }

    public static boolean wearsSkyMetal(LivingEntity entity) {
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.is(ModTags.Items.NIMBUSITE_ARMOR) || stack.is(ModTags.Items.LEVITITE_ARMOR)) return true;
        }
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && level().getDifficulty() == Difficulty.PEACEFUL) {
            discard();
            return;
        }
        if (!level().isClientSide() && getTarget() != null && !wearsSkyMetal(getTarget())) setTarget(null);
        AirSteering.faceMotion(this, isDiving() ? 25.0F : 8.0F);
    }

    //? if <26.1 {
    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }
    //?}

    @Override
    public SoundSource getSoundSource() {
        return SoundSource.HOSTILE;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return MobSounds.DUSKWING_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return MobSounds.DUSKWING_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return MobSounds.DUSKWING_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    // The dive ends almost straight above the target's head, where the shield's front/back check flips on a few
    // hundredths of a block: the hit is sourced from where the swoop came from instead
    private boolean strike(LivingEntity target, Vec3 from) {
        DamageSource source = new DamageSource(damageSources().mobAttack(this).typeHolder(), this, this, from);
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        ServerLevel level = (ServerLevel) level();
        //? if >=26.1 {
        /*Vec3 oldMovement = target.getDeltaMovement();
        boolean hurt = target.hurtServer(level, source, damage);
        if (hurt) causeExtraKnockback(target, getKnockback(target, source), oldMovement);
        *///?} else {
        boolean hurt = target.hurt(source, damage);
        float knockback = getKnockback(target, source);
        if (hurt && knockback > 0.0F) {
            target.knockback(knockback * 0.5F, Mth.sin(getYRot() * Mth.DEG_TO_RAD), -Mth.cos(getYRot() * Mth.DEG_TO_RAD));
            setDeltaMovement(getDeltaMovement().multiply(0.6, 1.0, 0.6));
        }
        //?}
        if (hurt) {
            EnchantmentHelper.doPostAttackEffects(level, target, source);
            setLastHurtMob(target);
            playAttackSound();
        }
        return hurt;
    }

    private enum Phase {
        CIRCLE, DIVE, CLIMB
    }

    private class DiveAttackGoal extends Goal {
        private Phase phase = Phase.CIRCLE;
        private int phaseTicks;
        private int circleDuration;
        private double angle;
        private Vec3 retreat = Vec3.ZERO;
        private Vec3 swoopFrom = Vec3.ZERO;

        DiveAttackGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            LivingEntity target = getTarget();
            return target != null && target.isAlive();
        }

        @Override
        public boolean canContinueToUse() {
            return canUse();
        }

        @Override
        public void start() {
            enter(Phase.CIRCLE);
            angle = getRandom().nextDouble() * Math.PI * 2;
        }

        @Override
        public void stop() {
            entityData.set(DIVING, false);
        }

        private void enter(Phase next) {
            phase = next;
            phaseTicks = 0;
            entityData.set(DIVING, next == Phase.DIVE);
            if (next == Phase.CIRCLE) circleDuration = 50 + getRandom().nextInt(50);
            if (next == Phase.DIVE) swoopFrom = position();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = getTarget();
            if (target == null) return;
            phaseTicks++;
            switch (phase) {
                case CIRCLE -> {
                    angle += 0.09;
                    double radius = 6.5;
                    double height = target.getY() + 7.0 + Math.sin(phaseTicks * 0.15);
                    getMoveControl().setWantedPosition(target.getX() + Math.cos(angle) * radius, height,
                            target.getZ() + Math.sin(angle) * radius, 1.0);
                    boolean above = getY() > target.getY() + 3.0 && distanceToSqr(target.getX(), getY(), target.getZ()) < 18 * 18;
                    if (phaseTicks > circleDuration && above && hasLineOfSight(target)) enter(Phase.DIVE);
                }
                case DIVE -> {
                    getMoveControl().setWantedPosition(target.getX(), target.getY(0.5), target.getZ(), 1.7);
                    getLookControl().setLookAt(target, 30.0F, 30.0F);
                    if (distanceToSqr(target.getX(), getY(), target.getZ()) > 2.0 * 2.0) swoopFrom = position();
                    if (getBoundingBox().inflate(0.35).intersects(target.getBoundingBox())) {
                        strike(target, swoopFrom);
                        Vec3 away = position().subtract(target.position()).multiply(1, 0, 1);
                        retreat = (away.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : away.normalize()).scale(10);
                        enter(Phase.CLIMB);
                    } else if (phaseTicks > 40 || horizontalCollision) {
                        enter(Phase.CIRCLE);
                    }
                }
                case CLIMB -> {
                    getMoveControl().setWantedPosition(target.getX() + retreat.x, target.getY() + 11.0, target.getZ() + retreat.z, 1.2);
                    if (phaseTicks > 25 || horizontalCollision) enter(Phase.CIRCLE);
                }
            }
        }
    }
}
