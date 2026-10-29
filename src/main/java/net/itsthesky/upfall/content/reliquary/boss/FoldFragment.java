package net.itsthesky.upfall.content.reliquary.boss;

import net.itsthesky.upfall.compat.mc.McEntities;
import net.itsthesky.upfall.content.quantum.QuantumParticles;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FoldFragment extends Projectile {
    private static final int OUTBOUND_TICKS = 25;
    private static final int MAX_LIFE = 140;
    private static final double SPEED = 0.9;
    private static final float DAMAGE = 6.6F;

    private final Set<UUID> struck = new HashSet<>();
    private Vec3 target = Vec3.ZERO;
    private boolean returning;

    public FoldFragment(EntityType<? extends FoldFragment> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        setNoGravity(true);
    }

    void launch(FoldWarden owner, Vec3 from, Vec3 target) {
        setOwner(owner);
        McEntities.snapTo(this, from.x, from.y, from.z, 0.0F, 0.0F);
        this.target = target;
        setDeltaMovement(target.subtract(from).normalize().scale(SPEED));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = getDeltaMovement();
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        if (!(level() instanceof ServerLevel server)) return;
        if (!(getOwner() instanceof FoldWarden owner) || !owner.isAlive() || tickCount > MAX_LIFE) {
            discard();
            return;
        }
        if (!returning && (tickCount > OUTBOUND_TICKS || position().distanceToSqr(target) < 1.0)) {
            returning = true;
            struck.clear();
        }
        if (returning) {
            Vec3 home = owner.position().add(0.0, owner.getBbHeight() / 2.0, 0.0);
            Vec3 toHome = home.subtract(position());
            if (toHome.lengthSqr() < 2.25) {
                discard();
                return;
            }
            Vec3 wanted = toHome.normalize().scale(SPEED * 1.3);
            setDeltaMovement(motion.add(wanted.subtract(motion).scale(0.25)));
        }
        DamageSource source = damageSources().mobProjectile(this, owner);
        for (Player player : server.getEntitiesOfClass(Player.class, getBoundingBox().inflate(0.3),
                p -> p.isAlive() && !p.isSpectator() && !p.isCreative() && !struck.contains(p.getUUID()))) {
            struck.add(player.getUUID());
            player.hurt(source, DAMAGE);
        }
        if (tickCount % 2 == 0) {
            server.sendParticles(QuantumParticles.QUANTUM_SPARK.get(), getX(), getY(0.5), getZ(), 1, 0.1, 0.1, 0.1, 0.0);
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return false;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }
}
