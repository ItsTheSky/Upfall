package net.itsthesky.upfall.compat;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.content.quantum.QuantumRecipeTypes;
import net.itsthesky.upfall.content.quantum.pedestal.QuantumIncantationRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RecipesReceivedEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

import java.util.List;

@EventBusSubscriber(modid = Upfall.MODID)
public final class IncantationRecipeSync {
    private static volatile List<RecipeHolder<QuantumIncantationRecipe>> received = List.of();

    private IncantationRecipeSync() {}

    public static List<RecipeHolder<QuantumIncantationRecipe>> received() {
        return received;
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        event.sendRecipes(QuantumRecipeTypes.INCANTATION.get());
    }

    @EventBusSubscriber(modid = Upfall.MODID, value = Dist.CLIENT)
    public static final class Client {
        private Client() {}

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onRecipesReceived(RecipesReceivedEvent event) {
            received = List.copyOf(event.getRecipeMap().byType(QuantumRecipeTypes.INCANTATION.get()));
        }
    }
}
