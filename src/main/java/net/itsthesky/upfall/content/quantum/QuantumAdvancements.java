package net.itsthesky.upfall.content.quantum;

import net.itsthesky.upfall.Upfall;
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
            {"gas_cave", "Holding Your Breath", "Find a cave filled with Veil Gas",
                    "Retenir son souffle", "Trouvez une grotte remplie de Gaz voilé"},
            {"flicker_shard", "Don't Blink", "Mine a Flicker Shard while keeping an eye on it",
                    "Ne clignez pas des yeux", "Minez un Éclat vacillant sans le quitter des yeux"},
            {"shifting_vault", "Caught Red-Handed", "Open the chest of a Shifting Vault",
                    "Pris la main dans le sac", "Ouvrez le coffre d'une Chambre forte changeante"},
            {"quantum_incantation", "Spooky Action", "Complete an incantation on a Quantum Pedestal",
                    "Action fantôme", "Réussissez une incantation sur un Piédestal quantique"},
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
        save(saver, enter, "gas_cave", QuantumItems.BOTTLED_VEIL_GAS.get(), EnterBlockTrigger.TriggerInstance.entersBlock(QuantumBlocks.VEIL_GAS.get()));
        save(saver, enter, "flicker_shard", QuantumItems.FLICKER_SHARD.get(), InventoryChangeTrigger.TriggerInstance.hasItems(QuantumItems.FLICKER_SHARD.get()));
        save(saver, enter, "shifting_vault", QuantumBlocks.SHIFTING_CHEST.get(), QuantumTriggers.VAULT_OPENED.get().criterion());
        save(saver, enter, "quantum_incantation", QuantumBlocks.QUANTUM_PEDESTAL.get(), QuantumTriggers.INCANTATION.get().criterion());
    }

    private static void save(Consumer<AdvancementHolder> saver, AdvancementHolder parent, String name, ItemLike icon, Criterion<?> criterion) {
        Advancement.Builder.advancement().parent(parent)
                .display(icon, Component.translatable("advancements.upfall." + name + ".title"),
                        Component.translatable("advancements.upfall." + name + ".description"), null, AdvancementType.TASK, true, true, false)
                .addCriterion(name, criterion)
                .save(saver, Upfall.id("upfall/" + name).toString());
    }
}
