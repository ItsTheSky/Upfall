package net.itsthesky.upfall.content.quantum.client;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DelegateBlockStateModel;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class VeilGasModel extends DelegateBlockStateModel {
    private final Map<Direction, List<BakedQuad>> inset = new EnumMap<>(Direction.class);

    public VeilGasModel(BlockStateModel original) {
        super(original);
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, List<BlockStateModelPart> parts) {
        if (!VeilVision.isActive()) return;
        int mask = VeilGasFaces.exposedMask(level, pos);
        QuadCollection.Builder quads = new QuadCollection.Builder();
        for (Direction direction : Direction.values()) {
            if ((mask & 1 << direction.ordinal()) != 0) {
                for (BakedQuad quad : insetQuads(level, pos, state, direction, random)) {
                    quads.addUnculledFace(quad);
                }
            }
        }
        parts.add(new SimpleModelWrapper(quads.build(), false, delegate.particleMaterial(level, pos, state)));
    }

    private synchronized List<BakedQuad> insetQuads(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction direction, RandomSource random) {
        return inset.computeIfAbsent(direction, d -> {
            List<BlockStateModelPart> originals = new ArrayList<>();
            delegate.collectParts(level, pos, state, random, originals);
            List<BakedQuad> result = new ArrayList<>();
            for (BlockStateModelPart part : originals) {
                for (BakedQuad quad : part.getQuads(d)) {
                    BakedQuad.MaterialInfo info = quad.materialInfo();
                    result.add(new BakedQuad(
                            shifted(quad.position0(), d), shifted(quad.position1(), d), shifted(quad.position2(), d), shifted(quad.position3(), d),
                            quad.packedUV0(), quad.packedUV1(), quad.packedUV2(), quad.packedUV3(), quad.direction(),
                            new BakedQuad.MaterialInfo(info.sprite(), info.layer(), info.itemRenderType(), info.tintIndex(), false, 0, false),
                            quad.bakedNormals(), quad.bakedColors()));
                }
            }
            return List.copyOf(result);
        });
    }

    private static Vector3f shifted(Vector3fc position, Direction d) {
        return new Vector3f(position.x() - d.getStepX() * VeilGasFaces.INSET, position.y() - d.getStepY() * VeilGasFaces.INSET,
                position.z() - d.getStepZ() * VeilGasFaces.INSET);
    }
}
