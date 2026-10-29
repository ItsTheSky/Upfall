package net.itsthesky.upfall.datagen;

import net.itsthesky.upfall.Upfall;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class DataGenContext {
    private final GatherDataEvent event;
    private final DataGenerator generator;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public DataGenContext(GatherDataEvent event, CompletableFuture<HolderLookup.Provider> registries) {
        this.event = event;
        this.generator = event.getGenerator();
        this.registries = registries;
    }

    public PackOutput output() {
        return generator.getPackOutput();
    }

    public CompletableFuture<HolderLookup.Provider> registries() {
        return registries;
    }

    public <T extends DataProvider> T add(T provider) {
        return event.addProvider(provider);
    }

    public <T extends DataProvider> T addClient(T provider) {
        return event.addProvider(provider);
    }

    public DataProvider lootTables(String name, List<LootTableProvider.SubProviderEntry> entries) {
        return new Named(name, new LootTableProvider(output(), Set.of(), entries, registries));
    }

    public DataProvider advancements(String name, List<AdvancementSubProvider> subProviders) {
        return new Named(name, new AdvancementProvider(output(), registries, subProviders));
    }

    public <T> void tags(ResourceKey<? extends Registry<T>> registry, String name, Consumer<TagSink<T>> body) {
        add(new KeyTags<>(output(), registry, registries, name, body));
    }

    private static final class KeyTags<T> extends KeyTagProvider<T> {
        private final String name;
        private final Consumer<TagSink<T>> body;

        KeyTags(PackOutput output, ResourceKey<? extends Registry<T>> registry, CompletableFuture<HolderLookup.Provider> registries,
                String name, Consumer<TagSink<T>> body) {
            super(output, registry, registries, Upfall.MODID);
            this.name = name;
            this.body = body;
        }

        @Override
        protected void addTags(HolderLookup.Provider provider) {
            body.accept(new TagSink<T>() {
                @Override
                public void put(TagKey<T> tag, ResourceKey<T> key) {
                    KeyTags.this.tag(tag).add(key);
                }

                @Override
                public void putTag(TagKey<T> tag, TagKey<T> child) {
                    KeyTags.this.tag(tag).addTag(child);
                }
            });
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private record Named(String name, DataProvider delegate) implements DataProvider {
        @Override
        public CompletableFuture<?> run(CachedOutput output) {
            return delegate.run(output);
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
