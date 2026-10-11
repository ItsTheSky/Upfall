package net.itsthesky.upfall.content.enchanting;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.registry.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class EnchantingEffects {
    private EnchantingEffects() {}

    public static final double MIST_DRAG = 0.8;
    private static final double SKYWALKER_SPEED_PER_LEVEL = 0.45;
    private static final ResourceLocation SKYWALKER_MODIFIER = Upfall.id("enchantment.skywalker");

    public static int level(LivingEntity entity, ResourceKey<Enchantment> key) {
        return entity.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT).get(key)
                .map(holder -> EnchantmentHelper.getEnchantmentLevel(holder, entity)).orElse(0);
    }

    public static boolean hasCloudsight(LivingEntity entity) {
        return level(entity, EnchantingModule.CLOUDSIGHT) > 0;
    }

    public static double mistDrag(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return MIST_DRAG;
        if (hasCloudsight(living)) return 1.0;
        double cancelled = Math.min(1.0, level(living, EnchantingModule.SKYWALKER) * 0.34);
        return MIST_DRAG + (1.0 - MIST_DRAG) * cancelled;
    }

    public static void updateSkywalker(LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;
        int level = entity.getItemBySlot(EquipmentSlot.FEET).isEmpty() ? 0
                : level(entity, EnchantingModule.SKYWALKER);
        if (level > 0 && !entity.level().getBlockState(entity.blockPosition()).is(ModBlocks.MIST.get())) {
            level = 0;
        }
        if (level == 0) {
            if (speed.hasModifier(SKYWALKER_MODIFIER)) speed.removeModifier(SKYWALKER_MODIFIER);
            return;
        }
        double amount = level * SKYWALKER_SPEED_PER_LEVEL;
        AttributeModifier current = speed.getModifier(SKYWALKER_MODIFIER);
        if (current == null || current.amount() != amount) {
            speed.addOrUpdateTransientModifier(new AttributeModifier(SKYWALKER_MODIFIER, amount,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof LivingEntity living && !living.level().isClientSide) {
            updateSkywalker(living);
        }
    }

    public static void onJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        int level = level(entity, EnchantingModule.JUMPSTART);
        if (level <= 0) return;
        Vec3 motion = entity.getDeltaMovement();
        double boost = 1.0 + 0.1 * level;
        entity.setDeltaMovement(motion.x * boost, motion.y, motion.z * boost);
    }
}
