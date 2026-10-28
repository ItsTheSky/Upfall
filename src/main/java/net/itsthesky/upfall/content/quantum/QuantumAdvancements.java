package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.registry.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

final class QuantumAdvancements {
    private QuantumAdvancements() {}

    static final String[][] LANG = {
            {"gas_cave", "Holding Your Breath", "Find a cave filled with Veil Gas; a Veil Mask from the Rustworks lets you breathe there",
                    "Retenir son souffle", "Trouvez une grotte remplie de Gaz voilé ; un Masque voilé des Rustworks vous permet d'y respirer"},
            {"flicker_shard", "Don't Blink", "Mine a Flicker Shard in a gas cave while keeping an eye on it",
                    "Ne clignez pas des yeux", "Minez un Éclat vacillant dans une grotte de Gaz voilé sans le quitter des yeux"},
            {"shifting_vault", "Caught Red-Handed", "Open the chest of a Shifting Vault and take its Quantum Pedestal Blueprint",
                    "Pris la main dans le sac", "Ouvrez le coffre d'une Chambre forte changeante et emportez son Plan de Piédestal quantique"},
            {"quantum_pedestal", "Superposition", "Smith a Quantum Pedestal from its blueprint, a Pedestal and a Quantum Stone",
                    "Superposition", "Forgez un Piédestal quantique avec son plan, un Piédestal et une Pierre quantique"},
            {"quantum_incantation", "Spooky Action", "Complete an incantation on a Quantum Pedestal to empower a piece of Levitite gear",
                    "Action fantôme", "Réussissez une incantation sur un Piédestal quantique pour renforcer une pièce d'équipement en levitite"},
    };

    static DataProvider provider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper helper) {
        AdvancementProvider provider = new AdvancementProvider(output, registries, helper, List.of(QuantumAdvancements::generate));
        return new DataProvider() {
            @Override
            public CompletableFuture<?> run(CachedOutput cache) {
                return provider.run(cache);
            }

            @Override
            public String getName() {
                return "Quantum advancements";
            }
        };
    }

    private static void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver, ExistingFileHelper helper) {
        AdvancementHolder enter = AdvancementSubProvider.createPlaceholder("upfall:upfall/enter");
        AdvancementHolder gas = save(saver, enter, "gas_cave", QuantumItems.BOTTLED_VEIL_GAS.get(), EnterBlockTrigger.TriggerInstance.entersBlock(QuantumBlocks.VEIL_GAS.get()));
        save(saver, gas, "flicker_shard", QuantumItems.FLICKER_SHARD.get(), InventoryChangeTrigger.TriggerInstance.hasItems(QuantumItems.FLICKER_SHARD.get()));
        AdvancementHolder vault = save(saver, enter, "shifting_vault", QuantumBlocks.SHIFTING_CHEST.get(), QuantumTriggers.VAULT_OPENED.get().criterion());
        AdvancementHolder pedestal = save(saver, vault, "quantum_pedestal", QuantumBlocks.QUANTUM_PEDESTAL.get(),
                InventoryChangeTrigger.TriggerInstance.hasItems(QuantumBlocks.QUANTUM_PEDESTAL.get()));
        save(saver, pedestal, "quantum_incantation", ModItems.LEVITITE_SWORD.get(), AdvancementType.GOAL, QuantumTriggers.INCANTATION.get().criterion());
    }

    private static AdvancementHolder save(Consumer<AdvancementHolder> saver, AdvancementHolder parent, String name, ItemLike icon, Criterion<?> criterion) {
        return save(saver, parent, name, icon, AdvancementType.TASK, criterion);
    }

    private static AdvancementHolder save(Consumer<AdvancementHolder> saver, AdvancementHolder parent, String name, ItemLike icon, AdvancementType type,
                                          Criterion<?> criterion) {
        return Advancement.Builder.advancement().parent(parent)
                .display(icon, Component.translatable("advancements.upfall." + name + ".title"),
                        Component.translatable("advancements.upfall." + name + ".description"), null, type, true, true, false)
                .addCriterion(name, criterion)
                .save(saver, Upfall.id("upfall/" + name).toString());
    }
}
