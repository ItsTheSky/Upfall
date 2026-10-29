package net.itsthesky.upfall.datagen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public interface TagSink<T> {
    void put(TagKey<T> tag, ResourceKey<T> key);

    void putTag(TagKey<T> tag, TagKey<T> child);

    default Appender<T> tag(TagKey<T> tag) {
        return new Appender<>(this, tag);
    }

    final class Appender<T> {
        private final TagSink<T> sink;
        private final TagKey<T> tag;

        private Appender(TagSink<T> sink, TagKey<T> tag) {
            this.sink = sink;
            this.tag = tag;
        }

        @SafeVarargs
        public final Appender<T> add(ResourceKey<T>... keys) {
            for (ResourceKey<T> key : keys) {
                sink.put(tag, key);
            }
            return this;
        }

        public Appender<T> addTag(TagKey<T> child) {
            sink.putTag(tag, child);
            return this;
        }
    }
}
