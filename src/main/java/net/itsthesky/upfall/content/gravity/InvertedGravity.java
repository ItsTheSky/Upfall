package net.itsthesky.upfall.content.gravity;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class InvertedGravity {
    private InvertedGravity() {}

    public static final ResourceLocation MODIFIER_ID = Upfall.id("inverted_gravity");
    public static final int CEILING_RANGE = 24;
    public static final double INVERTED_GRAVITY = -0.06;
    public static final int RELEASE_SLOW_FALLING_TICKS = 120;
    public static final int FLIP_TICKS = 8;
    private static final double CONTACT = 1.0E-3;

    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Upfall.MODID);
    public static final Supplier<AttachmentType<State>> STATE = ATTACHMENT_TYPES.register("gravity_state", () -> AttachmentType.builder(State::new).build());

    public static final class State {
        private boolean inverted;
        private float flip;
        private float flipO;
        private double lastX;
        private double lastZ;
        private double stepDistance;
    }

    public static void register(IEventBus modEventBus) {
        ATTACHMENT_TYPES.register(modEventBus);
        NeoForge.EVENT_BUS.addListener(InvertedGravity::onPlayerTickPre);
        NeoForge.EVENT_BUS.addListener(InvertedGravity::onPlayerTickPost);
        NeoForge.EVENT_BUS.addListener(InvertedGravity::onJump);
        NeoForge.EVENT_BUS.addListener(InvertedGravity::onSize);
    }

    public static boolean isInverted(Entity entity) {
        State state = entity.getExistingDataOrNull(STATE);
        return state != null && state.inverted;
    }

    public static float flipProgress(Entity entity, float partialTick) {
        State state = entity == null ? null : entity.getExistingDataOrNull(STATE);
        return state == null ? 0.0F : Mth.lerp(partialTick, state.flipO, state.flip);
    }

    public static boolean shouldInvert(Player player) {
        if (player.isSpectator() || player.getAbilities().flying || player.isFallFlying() || player.isPassenger()
                || player.isSleeping() || player.isShiftKeyDown() || player.isNoGravity()
                || player.isInWater() || player.isInLava() || !player.hasEffect(ModEffects.WEIGHTLESS)) {
            return false;
        }
        return hasCeiling(player.level(), player.getBoundingBox(), CEILING_RANGE);
    }

    public static boolean hasCeiling(BlockGetter level, AABB box, int range) {
        int minX = Mth.floor(box.minX + CONTACT);
        int maxX = Mth.floor(box.maxX - CONTACT);
        int minZ = Mth.floor(box.minZ + CONTACT);
        int maxZ = Mth.floor(box.maxZ - CONTACT);
        int startY = Mth.floor(box.maxY);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int y = startY; y <= startY + range; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockState state = level.getBlockState(cursor.set(x, y, z));
                    if (!state.isAir() && !state.getCollisionShape(level, cursor).isEmpty()) return true;
                }
            }
        }
        return false;
    }

    public static boolean isTouchingCeiling(Entity entity) {
        AABB box = entity.getBoundingBox();
        return !entity.level().noCollision(entity, new AABB(box.minX, box.maxY, box.minZ, box.maxX, box.maxY + CONTACT, box.maxZ));
    }

    public static boolean isSupportedBelow(Entity entity) {
        AABB box = entity.getBoundingBox();
        return !entity.level().noCollision(entity, new AABB(box.minX, box.minY - CONTACT, box.minZ, box.maxX, box.minY, box.maxZ));
    }

    private static boolean isRemote(Player player) {
        return player.level().isClientSide() && !player.isLocalPlayer();
    }

    private static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        State state = player.getData(STATE);
        boolean invert;
        if (isRemote(player)) {
            AttributeInstance gravity = player.getAttribute(Attributes.GRAVITY);
            invert = gravity != null && gravity.hasModifier(MODIFIER_ID);
        } else {
            invert = updateModifier(player, shouldInvert(player));
        }
        if (invert != state.inverted) {
            state.inverted = invert;
            player.refreshDimensions();
            if (!isRemote(player)) {
                player.resetFallDistance();
                if (invert) {
                    state.lastX = player.getX();
                    state.lastZ = player.getZ();
                } else {
                    release(player);
                }
            }
        }
        if (player.level().isClientSide()) {
            state.flipO = state.flip;
            state.flip = Mth.approach(state.flip, invert ? 1.0F : 0.0F, 1.0F / FLIP_TICKS);
        }
        if (invert && !isRemote(player)) {
            settle(player);
        }
    }

    private static void onPlayerTickPost(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        State state = player.getExistingDataOrNull(STATE);
        if (state == null || !state.inverted || isRemote(player)) return;
        settle(player);
        double dx = player.getX() - state.lastX;
        double dz = player.getZ() - state.lastZ;
        state.lastX = player.getX();
        state.lastZ = player.getZ();
        if (player.onGround()) {
            stepOnCeiling(player, state, Math.sqrt(dx * dx + dz * dz));
        }
    }

    private static boolean updateModifier(Player player, boolean invert) {
        AttributeInstance gravity = player.getAttribute(Attributes.GRAVITY);
        if (gravity == null) return false;
        AttributeModifier current = gravity.getModifier(MODIFIER_ID);
        if (invert) {
            double without = current == null ? gravity.getValue() : gravity.getValue() / (1.0 + current.amount());
            if (Math.abs(without) > 1.0E-4) {
                double amount = INVERTED_GRAVITY / without - 1.0;
                if (current == null || Math.abs(current.amount() - amount) > 1.0E-6) {
                    gravity.addOrUpdateTransientModifier(new AttributeModifier(MODIFIER_ID, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
                }
                return true;
            }
        }
        if (current != null) {
            gravity.removeModifier(MODIFIER_ID);
        }
        return false;
    }

    private static void settle(Player player) {
        player.resetFallDistance();
        player.setOnGround(isTouchingCeiling(player));
    }

    private static void release(Player player) {
        boolean supported = isSupportedBelow(player);
        player.setOnGround(supported);
        if (!supported && !player.level().isClientSide() && !player.isSpectator()) {
            player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, RELEASE_SLOW_FALLING_TICKS, 0, false, false, true));
        }
    }

    private static void stepOnCeiling(Player player, State state, double moved) {
        if (moved > 2.0) return;
        state.stepDistance += moved * 0.6;
        if (state.stepDistance < 1.0) return;
        state.stepDistance = 0.0;
        BlockPos pos = BlockPos.containing(player.getX(), player.getBoundingBox().maxY + 0.01, player.getZ());
        BlockState ceiling = player.level().getBlockState(pos);
        if (ceiling.isAir()) return;
        SoundType sound = ceiling.getSoundType(player.level(), pos, player);
        player.playSound(sound.getStepSound(), sound.getVolume() * 0.15F, sound.getPitch());
    }

    private static void onJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        if (isInverted(entity)) {
            Vec3 motion = entity.getDeltaMovement();
            entity.setDeltaMovement(motion.x, -Math.abs(motion.y), motion.z);
        }
    }

    private static void onSize(EntityEvent.Size event) {
        if (isInverted(event.getEntity())) {
            EntityDimensions size = event.getNewSize();
            event.setNewSize(size.withEyeHeight(size.height() - size.eyeHeight()));
        }
    }
}
