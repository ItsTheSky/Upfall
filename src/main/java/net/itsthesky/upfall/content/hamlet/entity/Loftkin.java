package net.itsthesky.upfall.content.hamlet.entity;

import net.itsthesky.upfall.content.hamlet.HamletSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class Loftkin extends AbstractVillager {
    private static final EntityDataAccessor<Integer> DATA_VARIANT = SynchedEntityData.defineId(Loftkin.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_PARASOL = SynchedEntityData.defineId(Loftkin.class, EntityDataSerializers.BOOLEAN);
    public static final int VARIANTS = 3;
    public static final byte EVENT_WAVE = 71;
    private static final int HOME_RADIUS = 30;
    private static final long RESTOCK_INTERVAL = 24000L;

    public final AnimationState waveAnimation = new AnimationState();
    @Nullable
    private BlockPos home;
    private int lostTicks;
    private int greetCooldown = 100;
    private long lastRestock;

    public Loftkin(EntityType<? extends Loftkin> type, Level level) {
        super(type, level);
        if (getNavigation() instanceof GroundPathNavigation navigation) {
            navigation.setCanOpenDoors(true);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        goalSelector.addGoal(2, new OpenDoorGoal(this, true));
        goalSelector.addGoal(3, new LoftkinGoals.GreetVisitor(this));
        goalSelector.addGoal(5, new LoftkinGoals.PlatformStroll(this, 0.42));
        goalSelector.addGoal(8, new InteractGoal(this, Player.class, 3.0F, 1.0F));
        goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_VARIANT, 0);
        builder.define(DATA_PARASOL, false);
    }

    public int getVariant() {
        return entityData.get(DATA_VARIANT);
    }

    public void setVariant(int variant) {
        entityData.set(DATA_VARIANT, Math.floorMod(variant, VARIANTS));
    }

    public boolean hasParasol() {
        return entityData.get(DATA_PARASOL);
    }

    @Nullable
    public BlockPos getHome() {
        return home;
    }

    public void setHome(BlockPos home) {
        this.home = home.immutable();
    }

    int getGreetCooldown() {
        return greetCooldown;
    }

    void setGreetCooldown(int ticks) {
        greetCooldown = ticks;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data) {
        setVariant(random.nextInt(VARIANTS));
        if (home == null) {
            setHome(blockPosition());
        }
        return super.finalizeSpawn(level, difficulty, spawnType, data);
    }

    @Override
    public int getMaxFallDistance() {
        return 1;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            return;
        }
        if (greetCooldown > 0) {
            greetCooldown--;
        }
        if (getUnhappyCounter() > 0) {
            setUnhappyCounter(getUnhappyCounter() - 1);
        }
        updateParasol();
        if (home != null && !isTrading() && !isPassenger()) {
            boolean lost = getY() < home.getY() - 8 || distanceToSqr(Vec3.atBottomCenterOf(home)) > HOME_RADIUS * HOME_RADIUS;
            lostTicks = lost ? lostTicks + 1 : 0;
            if (lostTicks > 80) {
                returnHome();
            }
        }
        long time = level().getGameTime();
        if (time - lastRestock > RESTOCK_INTERVAL && offers != null) {
            offers.forEach(MerchantOffer::resetUses);
            lastRestock = time;
        }
    }

    private void updateParasol() {
        boolean parasol = hasParasol();
        Vec3 motion = getDeltaMovement();
        if (!parasol && !onGround() && !isInWater() && motion.y < -0.3) {
            parasol = true;
            playSound(HamletSounds.LOFTKIN_PARASOL.get(), 0.8F, 1.0F);
        } else if (parasol && (onGround() || isInWater())) {
            parasol = false;
        }
        if (parasol && motion.y < -0.12) {
            setDeltaMovement(motion.x * 0.9, -0.12, motion.z * 0.9);
            resetFallDistance();
        }
        entityData.set(DATA_PARASOL, parasol);
    }

    public void returnHome() {
        if (home == null || !(level() instanceof ServerLevel level)) {
            return;
        }
        level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1.0, getZ(), 16, 0.3, 0.6, 0.3, 0.02);
        getNavigation().stop();
        setDeltaMovement(Vec3.ZERO);
        teleportTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
        resetFallDistance();
        entityData.set(DATA_PARASOL, false);
        level.sendParticles(ParticleTypes.CLOUD, getX(), getY() + 1.0, getZ(), 16, 0.3, 0.6, 0.3, 0.02);
        level.playSound(null, blockPosition(), HamletSounds.LOFTKIN_PARASOL.get(), SoundSource.NEUTRAL, 0.8F, 1.4F);
        lostTicks = 0;
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_WAVE) {
            waveAnimation.start(tickCount);
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof net.minecraft.world.item.SpawnEggItem || !isAlive() || isTrading() || isBaby()) {
            return super.mobInteract(player, hand);
        }
        if (hand == InteractionHand.MAIN_HAND) {
            player.awardStat(Stats.TALKED_TO_VILLAGER);
        }
        if (!level().isClientSide) {
            if (getOffers().isEmpty()) {
                setUnhappyCounter(40);
                playSound(HamletSounds.LOFTKIN_NO.get(), getSoundVolume(), getVoicePitch());
                return InteractionResult.CONSUME;
            }
            setTradingPlayer(player);
            openTradingScreen(player, getDisplayName(), 1);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    protected void updateTrades() {
        LoftkinTrades.fill(this, getOffers(), random);
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        if (offer.shouldRewardExp()) {
            level().addFreshEntity(new ExperienceOrb(level(), getX(), getY() + 0.5, getZ(), 3 + random.nextInt(4)));
        }
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return HamletSounds.LOFTKIN_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return HamletSounds.LOFTKIN_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return HamletSounds.LOFTKIN_DEATH.get();
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean yes) {
        return yes ? HamletSounds.LOFTKIN_YES.get() : HamletSounds.LOFTKIN_NO.get();
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return HamletSounds.LOFTKIN_YES.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Variant", getVariant());
        tag.putLong("LastRestock", lastRestock);
        if (home != null) {
            tag.put("Home", NbtUtils.writeBlockPos(home));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setVariant(tag.getInt("Variant"));
        lastRestock = tag.getLong("LastRestock");
        NbtUtils.readBlockPos(tag, "Home").ifPresent(this::setHome);
    }
}
