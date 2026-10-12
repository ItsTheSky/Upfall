package net.itsthesky.upfall.content.quantum;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.itsthesky.upfall.Upfall;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

public final class QuantumTriggers {
    private QuantumTriggers() {}

    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, Upfall.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, Simple> VAULT_OPENED = TRIGGERS.register("shifting_vault_opened", Simple::new);
    public static final DeferredHolder<CriterionTrigger<?>, Simple> INCANTATION = TRIGGERS.register("quantum_incantation", Simple::new);

    public static class Simple extends SimpleCriterionTrigger<Simple.Instance> {
        @Override
        public Codec<Instance> codec() {
            return Instance.CODEC;
        }

        public void trigger(ServerPlayer player) {
            trigger(player, instance -> true);
        }

        public Criterion<Instance> criterion() {
            return createCriterion(new Instance(Optional.empty()));
        }

        public record Instance(Optional<ContextAwarePredicate> player) implements SimpleCriterionTrigger.SimpleInstance {
            public static final Codec<Instance> CODEC = RecordCodecBuilder.create(i -> i.group(
                    EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player)
            ).apply(i, Instance::new));
        }
    }
}
