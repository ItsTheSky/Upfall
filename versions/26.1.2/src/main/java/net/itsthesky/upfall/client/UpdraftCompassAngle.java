package net.itsthesky.upfall.client;

import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.registry.ModDataComponents;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.NeedleDirectionHelper;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class UpdraftCompassAngle extends NeedleDirectionHelper implements RangeSelectItemModelProperty {
    public static final MapCodec<UpdraftCompassAngle> MAP_CODEC = MapCodec.unit(UpdraftCompassAngle::new);

    private final Wobbler wobbler = newWobbler(0.8F);
    private final Wobbler noTargetWobbler = newWobbler(0.8F);
    private final RandomSource random = RandomSource.create();

    public UpdraftCompassAngle() {
        super(true);
    }

    @Override
    protected float calculate(ItemStack itemStack, ClientLevel level, int seed, ItemOwner owner) {
        GlobalPos target = itemStack.get(ModDataComponents.UPDRAFT_TARGET.get());
        long gameTime = level.getGameTime();
        return !isValidTarget(owner, target) ? randomlySpinningRotation(seed, gameTime) : rotationTowards(owner, gameTime, target.pos());
    }

    @Override
    public MapCodec<UpdraftCompassAngle> type() {
        return MAP_CODEC;
    }

    private float randomlySpinningRotation(int seed, long gameTime) {
        if (noTargetWobbler.shouldUpdate(gameTime)) {
            noTargetWobbler.update(gameTime, random.nextFloat());
        }
        return Mth.positiveModulo(noTargetWobbler.rotation() + hash(seed) / 2.1474836E9F, 1.0F);
    }

    private float rotationTowards(ItemOwner owner, long gameTime, BlockPos targetPos) {
        float angleToTarget = (float) angleFromOwnerToPos(owner, targetPos);
        float ownerYRotation = wrappedVisualRotationY(owner);
        float targetRotation;
        if (owner.asLivingEntity() instanceof Player player && player.isLocalPlayer() && player.level().tickRateManager().runsNormally()) {
            if (wobbler.shouldUpdate(gameTime)) {
                wobbler.update(gameTime, 0.5F - (ownerYRotation - 0.25F));
            }
            targetRotation = angleToTarget + wobbler.rotation();
        } else {
            targetRotation = 0.5F - (ownerYRotation - 0.25F - angleToTarget);
        }
        return Mth.positiveModulo(targetRotation, 1.0F);
    }

    private static boolean isValidTarget(ItemOwner owner, @Nullable GlobalPos target) {
        return target != null
                && target.dimension() == owner.level().dimension()
                && !(target.pos().distToCenterSqr(owner.position()) < 1.0E-5F);
    }

    private static double angleFromOwnerToPos(ItemOwner owner, BlockPos pos) {
        Vec3 target = Vec3.atCenterOf(pos);
        Vec3 ownerPosition = owner.position();
        return Math.atan2(target.z() - ownerPosition.z(), target.x() - ownerPosition.x()) / (float) (Math.PI * 2);
    }

    private static float wrappedVisualRotationY(ItemOwner owner) {
        return Mth.positiveModulo(owner.getVisualRotationYInDegrees() / 360.0F, 1.0F);
    }

    private static int hash(int value) {
        return value * 1327217883;
    }
}
