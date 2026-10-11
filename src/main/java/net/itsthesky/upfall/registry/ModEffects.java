package net.itsthesky.upfall.registry;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.gameplay.WeightlessEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
    private ModEffects() {}

    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, Upfall.MODID);

    public static final DeferredHolder<MobEffect, MobEffect> WEIGHTLESS = MOB_EFFECTS.register("weightless", () -> new WeightlessEffect()
            .addAttributeModifier(Attributes.GRAVITY, Upfall.id("effect.weightless.gravity"), -0.7, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)
            .addAttributeModifier(Attributes.JUMP_STRENGTH, Upfall.id("effect.weightless.jump"), 0.2, AttributeModifier.Operation.ADD_VALUE)
            .addAttributeModifier(Attributes.FALL_DAMAGE_MULTIPLIER, Upfall.id("effect.weightless.fall_damage"), -0.75, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
}
