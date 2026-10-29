package net.itsthesky.upfall.content.quantum.client;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class VeilGasModel extends BakedModelWrapper<BakedModel> {
    private static final ModelProperty<Integer> FACES = new ModelProperty<>();
    private final Map<Direction, List<BakedQuad>> inset = new EnumMap<>(Direction.class);

    public VeilGasModel(BakedModel original) {
        super(original);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData modelData) {
        return ModelData.builder().with(FACES, VeilGasFaces.exposedMask(level, pos)).build();
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData,
                                    @Nullable RenderType renderType) {
        if (!VeilVision.isActive() || side != null) return List.of();
        Integer mask = extraData.get(FACES);
        if (mask == null) mask = 0b111111;
        List<BakedQuad> quads = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if ((mask & 1 << direction.ordinal()) != 0) {
                quads.addAll(insetQuads(state, direction, rand));
            }
        }
        return quads;
    }

    private synchronized List<BakedQuad> insetQuads(@Nullable BlockState state, Direction direction, RandomSource rand) {
        return inset.computeIfAbsent(direction, d -> {
            List<BakedQuad> result = new ArrayList<>();
            for (BakedQuad quad : originalModel.getQuads(state, d, rand)) {
                int[] vertices = quad.getVertices().clone();
                int stride = vertices.length / 4;
                for (int v = 0; v < 4; v++) {
                    int i = v * stride;
                    vertices[i] = Float.floatToRawIntBits(Float.intBitsToFloat(vertices[i]) - d.getStepX() * VeilGasFaces.INSET);
                    vertices[i + 1] = Float.floatToRawIntBits(Float.intBitsToFloat(vertices[i + 1]) - d.getStepY() * VeilGasFaces.INSET);
                    vertices[i + 2] = Float.floatToRawIntBits(Float.intBitsToFloat(vertices[i + 2]) - d.getStepZ() * VeilGasFaces.INSET);
                }
                result.add(new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), false, false));
            }
            return List.copyOf(result);
        });
    }
}
