package net.itsthesky.upfall.content.machines;

import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class ModelBoxes {
    private ModelBoxes() {}

    public static Identifier create(BiConsumer<Identifier, ModelInstance> output, Identifier model, Map<String, Material> textures, List<ModelBox> boxes) {
        Map<String, TextureSlot> slots = new HashMap<>();
        TextureMapping mapping = new TextureMapping();
        ExtendedModelTemplateBuilder template = ExtendedModelTemplateBuilder.builder().parent(Identifier.withDefaultNamespace("block/block"));
        textures.forEach((name, material) -> {
            TextureSlot slot = TextureSlot.create(name);
            slots.put(name, slot);
            template.requiredTextureSlot(slot);
            mapping.put(slot, material);
        });
        for (ModelBox box : boxes) {
            template.element(element -> {
                element.from(box.x1(), box.y1(), box.z1()).to(box.x2(), box.y2(), box.z2());
                box.faces().forEach((direction, face) -> element.face(direction, builder -> {
                    builder.texture(slots.get(face.texture()));
                    if (face.uvs() != null) {
                        builder.uvs(face.uvs()[0], face.uvs()[1], face.uvs()[2], face.uvs()[3]);
                    }
                    if (face.cullface() != null) {
                        builder.cullface(face.cullface());
                    }
                }));
            });
        }
        return template.build().create(model, mapping, output);
    }
}
