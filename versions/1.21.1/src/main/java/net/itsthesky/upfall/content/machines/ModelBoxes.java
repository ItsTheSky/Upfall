package net.itsthesky.upfall.content.machines;

import net.neoforged.neoforge.client.model.generators.ModelBuilder;

import java.util.List;

public final class ModelBoxes {
    private ModelBoxes() {}

    public static void apply(ModelBuilder<?> model, List<ModelBox> boxes) {
        for (ModelBox box : boxes) {
            var element = model.element().from(box.x1(), box.y1(), box.z1()).to(box.x2(), box.y2(), box.z2());
            box.faces().forEach((direction, face) -> {
                var builder = element.face(direction).texture("#" + face.texture());
                if (face.uvs() != null) {
                    builder.uvs(face.uvs()[0], face.uvs()[1], face.uvs()[2], face.uvs()[3]);
                }
                if (face.cullface() != null) {
                    builder.cullface(face.cullface());
                }
                builder.end();
            });
            element.end();
        }
    }
}
