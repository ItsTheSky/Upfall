package net.itsthesky.upfall.content.quantum.vault;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;

public final class VaultTunnel {
    private VaultTunnel() {}

    public static final int HEIGHT = 3;
    private static final double INSIDE = 4.0;
    private static final double LOOKAHEAD = 7.0;

    public record Path(List<BlockPos> main, List<BlockPos> branches, BlockPos exit) {
        public List<BlockPos> all() {
            List<BlockPos> all = new ArrayList<>(main);
            all.addAll(branches);
            return all;
        }
    }

    public static @Nullable Path dig(BlockPos door, BoundingBox avoid, BoundingBox reach, int minLength, int maxLength,
                                     RandomSource random, ToDoubleFunction<BlockPos> density) {
        List<BlockPos> main = new ArrayList<>();
        double x = door.getX() + 0.5;
        double z = door.getZ() + 0.5;
        int y = door.getY();
        double yaw = Math.PI / 2;
        double wander = 0.0;
        BlockPos exit = null;
        BlockPos last = null;
        for (int step = 0; step < maxLength; step++) {
            boolean seekEdge = step >= minLength;
            if (step >= 4) {
                wander = Mth.clamp(wander + (random.nextDouble() - 0.5) * 0.35, -0.3, 0.3);
                double best = Double.NaN;
                double bestYaw = yaw;
                for (double turn = -0.9; turn <= 0.91; turn += 0.45) {
                    double candidate = yaw + turn + wander;
                    BlockPos ahead = BlockPos.containing(x + Math.cos(candidate) * LOOKAHEAD, y + 1, z + Math.sin(candidate) * LOOKAHEAD);
                    if (avoid.isInside(ahead) || !inside(reach, ahead)) continue;
                    double d = density.applyAsDouble(ahead);
                    double score = seekEdge ? -d - Math.abs(turn) * 2.0 : (d >= INSIDE + 3.0 ? 10.0 : d) - Math.abs(turn) * 1.5;
                    if (Double.isNaN(best) || score > best) {
                        best = score;
                        bestYaw = candidate;
                    }
                }
                if (Double.isNaN(best)) return null;
                yaw = bestYaw;
            }
            x += Math.cos(yaw);
            z += Math.sin(yaw);
            if (step > 6 && random.nextFloat() < 0.06F) {
                int dy = random.nextBoolean() ? 1 : -1;
                if (Math.abs(y + dy - door.getY()) <= 6) y += dy;
            }
            BlockPos pos = BlockPos.containing(x, y, z);
            if (pos.equals(last)) continue;
            if (!inside(reach, pos)) return null;
            double d = density.applyAsDouble(pos.above());
            if (!seekEdge && d < INSIDE && step > 3) return null;
            main.add(pos);
            last = pos;
            if (seekEdge && d < 0.0) {
                exit = pos;
                break;
            }
        }
        if (exit == null) return null;

        List<BlockPos> branches = new ArrayList<>();
        int count = 2 + random.nextInt(2);
        for (int b = 0; b < count && main.size() > 30; b++) {
            BlockPos from = main.get(10 + random.nextInt(main.size() - 25));
            double angle = random.nextDouble() * Math.PI * 2.0;
            double bx = from.getX() + 0.5;
            double bz = from.getZ() + 0.5;
            int length = 8 + random.nextInt(12);
            for (int i = 0; i < length; i++) {
                angle += (random.nextDouble() - 0.5) * 0.6;
                bx += Math.cos(angle);
                bz += Math.sin(angle);
                BlockPos pos = BlockPos.containing(bx, from.getY(), bz);
                if (avoid.isInside(pos) || !inside(reach, pos) || density.applyAsDouble(pos.above()) < INSIDE + 1.0) break;
                branches.add(pos);
            }
        }
        return new Path(main, branches, exit);
    }

    private static boolean inside(BoundingBox reach, BlockPos pos) {
        return pos.getX() >= reach.minX() && pos.getX() <= reach.maxX() && pos.getZ() >= reach.minZ() && pos.getZ() <= reach.maxZ();
    }
}
