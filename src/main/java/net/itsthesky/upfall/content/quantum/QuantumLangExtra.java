package net.itsthesky.upfall.content.quantum;

import java.util.function.BiConsumer;

final class QuantumLangExtra {
    private QuantumLangExtra() {}

    private static final String[][] ENTRIES = {
            {"message.upfall.incantation.busy", "An incantation is already in progress", "Une incantation est déjà en cours"},
            {"message.upfall.incantation.no_piece", "Place a Levitite piece on the Quantum Pedestal first", "Posez d'abord une pièce en levitite sur le Piédestal quantique"},
            {"message.upfall.incantation.incomplete", "The Quantum Pedestal needs 8 Pedestals around it, %s blocks away",
                    "Le Piédestal quantique a besoin de 8 piédestaux autour de lui, à %s blocs"},
            {"message.upfall.incantation.no_match", "These offerings form no known incantation", "Ces offrandes ne forment aucune incantation connue"},
            {"message.upfall.incantation.not_applicable", "This piece cannot receive that upgrade", "Cette pièce ne peut pas recevoir cette amélioration"},
            {"message.upfall.incantation.duplicate", "This piece already carries that upgrade", "Cette pièce porte déjà cette amélioration"},
            {"message.upfall.incantation.full", "This piece cannot carry more than %s upgrades", "Cette pièce ne peut pas porter plus de %s améliorations"},
            {"message.upfall.incantation.collapsed", "You looked away: the incantation collapsed", "Vous avez détourné le regard: l'incantation s'est effondrée"},
            {"emi.category.upfall.quantum_incantation", "Quantum Incantation", "Incantation quantique"},
            {"compat.upfall.incantation.hint", "Look at the pedestal for %s s", "Regardez le piédestal pendant %s s"},
            {"message.upfall.rewind.triggered", "Time rewinds around you", "Le temps rembobine autour de vous"},
            {"block.upfall.pedestal.desc", "Holds one item. Ring a Quantum Pedestal with eight of them", "Contient un objet. Entourez un Piédestal quantique de huit d'entre eux"},
            {"block.upfall.quantum_pedestal.desc", "Sneak and use with an empty hand to start an incantation, then keep watching it",
                    "Accroupissez-vous et utilisez-le main vide pour lancer une incantation, puis ne le quittez pas des yeux"},
            {"upgrade.upfall.quantum.veilsight", "Veilsight", "Vue voilée"},
            {"upgrade.upfall.quantum.veilsight.desc", "Filters the Veil Gas and reveals it", "Filtre le Gaz voilé et le rend visible"},
            {"upgrade.upfall.quantum.uncertainty", "Uncertainty", "Incertitude"},
            {"upgrade.upfall.quantum.uncertainty.desc", "Chance to dodge incoming hits", "Chance d'esquiver les coups reçus"},
            {"upgrade.upfall.quantum.zipline", "Zipline", "Tyrolienne"},
            {"upgrade.upfall.quantum.zipline.desc", "Latch onto ziplines with an empty hand", "Accrochez-vous aux tyroliennes à main nue"},
            {"upgrade.upfall.quantum.anchored", "Anchored", "Ancré"},
            {"upgrade.upfall.quantum.anchored.desc", "Immune to knockback", "Insensible au recul"},
            {"upgrade.upfall.quantum.solidifier", "Solidifier", "Solidificateur"},
            {"upgrade.upfall.quantum.solidifier.desc", "Bounce on Mist, sneak to sink into it", "Rebondissez sur la Brume, accroupissez-vous pour y plonger"},
            {"upgrade.upfall.quantum.mosswalker", "Mosswalker", "Arpenteur de mousse"},
            {"upgrade.upfall.quantum.mosswalker.desc", "Run much faster on moss", "Courez bien plus vite sur la mousse"},
            {"upgrade.upfall.quantum.superposition", "Superposition", "Superposition"},
            {"upgrade.upfall.quantum.superposition.desc", "Mining an ore also mines an identical neighbor", "Miner un minerai mine aussi un minerai identique voisin"},
            {"upgrade.upfall.quantum.entanglement", "Entanglement", "Intrication"},
            {"upgrade.upfall.quantum.entanglement.desc", "Links the next two creatures you hit: they share their wounds",
                    "Lie les deux prochaines créatures frappées: elles partagent leurs blessures"},
            {"upgrade.upfall.quantum.rewind", "Rewind", "Rembobinage"},
            {"upgrade.upfall.quantum.rewind.desc", "Once a day, cheats death by rewinding to the last solid ground",
                    "Une fois par jour, trompe la mort en vous ramenant sur la dernière terre ferme"},
    };

    static void english(BiConsumer<String, String> add) {
        for (String[] entry : ENTRIES) add.accept(entry[0], entry[1]);
        for (String[] entry : QuantumAdvancements.LANG) {
            add.accept("advancements.upfall." + entry[0] + ".title", entry[1]);
            add.accept("advancements.upfall." + entry[0] + ".description", entry[2]);
        }
    }

    static void french(BiConsumer<String, String> add) {
        for (String[] entry : ENTRIES) add.accept(entry[0], entry[2]);
        for (String[] entry : QuantumAdvancements.LANG) {
            add.accept("advancements.upfall." + entry[0] + ".title", entry[3]);
            add.accept("advancements.upfall." + entry[0] + ".description", entry[4]);
        }
    }
}
