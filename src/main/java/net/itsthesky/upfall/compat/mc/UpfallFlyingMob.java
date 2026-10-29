package net.itsthesky.upfall.compat.mc;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
//? if <26.1 {
import net.minecraft.world.entity.FlyingMob;
//?}

//? if >=26.1 {
/*public abstract class UpfallFlyingMob extends Mob {
*///?} else {
public abstract class UpfallFlyingMob extends FlyingMob {
//?}
    protected UpfallFlyingMob(EntityType<? extends UpfallFlyingMob> type, Level level) {
        super(type, level);
    }
    //? if >=26.1 {
    /*@Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isInWater()) {
            this.moveRelative(0.02F, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8F));
        } else if (this.isInLava()) {
            this.moveRelative(0.02F, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
        } else {
            BlockPos ground = this.getBlockPosBelowThatAffectsMyMovement();
            float f = 0.91F;
            if (this.onGround()) {
                f = this.level().getBlockState(ground).getFriction(this.level(), ground, this) * 0.91F;
            }
            float f1 = 0.16277137F / (f * f * f);
            this.moveRelative(this.onGround() ? 0.1F * f1 : 0.02F, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale((double) f));
        }
    }

    @Override
    public boolean onClimbable() {
        return false;
    }
    *///?}
}
