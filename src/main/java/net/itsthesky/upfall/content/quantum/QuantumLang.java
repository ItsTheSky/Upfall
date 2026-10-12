package net.itsthesky.upfall.content.quantum;

import java.util.function.BiConsumer;

final class QuantumLang {
    private QuantumLang() {}

    private static final String[][] ENTRIES = {
            {"death.attack.upfall.veil_gas", "%1$s breathed the Veil", "%1$s a respiré le Voile"},
            {"death.attack.upfall.veil_gas.player", "%1$s breathed the Veil while fleeing %2$s", "%1$s a respiré le Voile en fuyant %2$s"},
            {"death.attack.upfall.entanglement", "%1$s was torn apart by an entangled wound", "%1$s a été déchiré par une blessure intriquée"},
            {"death.attack.upfall.entanglement.player", "%1$s shared a wound dealt by %2$s", "%1$s a partagé une blessure infligée par %2$s"},
            {"item.upfall.bottled_veil_gas.desc", "Use on a block to release the gas", "Utilisez sur un bloc pour libérer le gaz"},
            {"item.upfall.veil_mask.desc", "Filters the Veil Gas and reveals it", "Filtre le Gaz voilé et le rend visible"},
            {"item.upfall.quantum_stone.desc", "Corner stone of an incantation", "Pierre d'angle d'une incantation"},
            {"container.upfall.shifting_chest", "Shifting Chest", "Coffre changeant"},
            {"item.upfall.flicker_shard.desc", "Only stays still when watched", "Ne reste immobile que si on le regarde"},
    };

    static void english(BiConsumer<String, String> add) {
        for (String[] entry : ENTRIES) add.accept(entry[0], entry[1]);
        QuantumLangExtra.english(add);
    }

    static void french(BiConsumer<String, String> add) {
        for (String[] entry : ENTRIES) add.accept(entry[0], entry[2]);
        QuantumLangExtra.french(add);
    }
}
