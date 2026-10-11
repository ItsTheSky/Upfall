package net.itsthesky.upfall.content.engine;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.ArrayList;
import java.util.List;

public final class EngineBooks {
    private EngineBooks() {}

    public record Book(String id, String title, String author, List<String> pages) {
        public String pageKey(int page) {
            return "book.upfall.stalled_engine." + id + "." + (page + 1);
        }

        public ItemStack create() {
            List<Filterable<Component>> content = new ArrayList<>();
            for (int i = 0; i < pages.size(); i++) {
                content.add(Filterable.passThrough(Component.translatable(pageKey(i))));
            }
            ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
            stack.set(DataComponents.WRITTEN_BOOK_CONTENT, new WrittenBookContent(Filterable.passThrough(title), author, 0, content, true));
            return stack;
        }
    }

    public static final Book FOREMAN_LOG = new Book("foreman_log", "Pump Hall Log", "Foreman Ives", List.of(
            "Day 212. Pressure holds at the third gauge. The Horologists came again with their brass charts and asked for more flow toward the Spire.",
            "They say their tower turns with the days of the world below, and that it must drink to keep its hours. We send it what the clouds give us.",
            "Day 240. The Heartgear stutters. The vault is flooded with stagnant mist and sealed until the clockmakers answer.",
            "Should anyone need the vault: three drain valves feed the regulator. One in the west tank, one on the north gantry, one here in my office. Open all three."));

    public static final Book ENGINEER_NOTES = new Book("engineer_notes", "Notes on the Spire Main", "Third Horologist", List.of(
            "The Spire Main leaves this hall and runs straight to the Chronospire. We laid it true: even broken, the pipe still points home.",
            "Every phase of the Spire was tuned against this engine. Dawn, Noon, Dusk, Midnight: four strokes of a single heart.",
            "Should the Heartgear stop, the Spire will keep turning on its own memory. But it will forget the hands that wound it."));

    public static final Book VAULT_LETTER = new Book("vault_letter", "To the One Who Drains", "The Horologists", List.of(
            "If you read this, the mist is gone and the regulator still obeys. Good. The plans in this vault are ours.",
            "Kairite turns their gears. Mine it when the vein pulses brightest, or you will only gather dust.",
            "Carry the plans to a smithing table. And if you climb the Chronospire, tell it the engine is stalled, not dead."));

    public static List<Book> all() {
        return List.of(FOREMAN_LOG, ENGINEER_NOTES, VAULT_LETTER);
    }

    public static Book byId(String id) {
        return all().stream().filter(book -> book.id().equals(id)).findFirst().orElse(FOREMAN_LOG);
    }
}
