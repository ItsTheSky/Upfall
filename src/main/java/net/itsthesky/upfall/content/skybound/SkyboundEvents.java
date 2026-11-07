package net.itsthesky.upfall.content.skybound;

import net.itsthesky.upfall.Upfall;
import net.itsthesky.upfall.UpfallConfig;
import net.itsthesky.upfall.worldgen.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameRules;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
//? if >=26.1 {
/*import net.minecraft.world.level.storage.LevelData;
*///?}

public final class SkyboundEvents {
    private SkyboundEvents() {}

    public static final ResourceLocation HEALTH_MODIFIER = Upfall.id("skybound_health");

    static void register() {
        NeoForge.EVENT_BUS.addListener(SkyboundEvents::onEntityJoin);
        NeoForge.EVENT_BUS.addListener(SkyboundEvents::onIncomingDamage);
        NeoForge.EVENT_BUS.addListener(SkyboundEvents::onCreateSpawn);
        NeoForge.EVENT_BUS.addListener(SkyboundEvents::onServerStarted);
    }

    public static boolean isWeakened(Entity entity) {
        if (!(entity instanceof Enemy)) return false;
        var type = entity.getType().builtInRegistryHolder();
        return !type.is(Tags.EntityTypes.BOSSES) && !type.is(SkyboundData.UNSCALED);
    }

    private static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof LivingEntity living) || !isWeakened(living)) return;
        if (!ModDimensions.isSkybound(event.getLevel())) return;
        AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;
        double multiplier = UpfallConfig.getDouble(UpfallConfig.SKYBOUND_MOB_HEALTH);
        maxHealth.addOrReplacePermanentModifier(new AttributeModifier(HEALTH_MODIFIER, multiplier - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        if (living.getHealth() > living.getMaxHealth()) {
            living.setHealth(living.getMaxHealth());
        }
    }

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker == null || !isWeakened(attacker) || !ModDimensions.isSkybound(event.getEntity().level())) return;
        event.setAmount(event.getAmount() * (float) UpfallConfig.getDouble(UpfallConfig.SKYBOUND_MOB_DAMAGE));
    }

    private static void onCreateSpawn(LevelEvent.CreateSpawnPosition event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD || !ModDimensions.isUpfall(level)) return;
        BlockPos spawn = SkyboundSpawn.find(level);
        if (spawn == null) {
            Upfall.LOGGER.warn("No island found for the world spawn, falling back to the vanilla search");
            return;
        }
        //? if >=26.1 {
        /*event.getSettings().setSpawn(LevelData.RespawnData.of(level.dimension(), spawn, 0.0F, 0.0F));
        level.getGameRules().set(GameRules.RESPAWN_RADIUS, 0, level.getServer());
        *///?} else {
        event.getSettings().setSpawn(spawn, 0.0F);
        level.getGameRules().getRule(GameRules.RULE_SPAWN_RADIUS).set(0, level.getServer());
        //?}
        event.setCanceled(true);
    }

    private static void onServerStarted(ServerStartedEvent event) {
        SkyboundPack.enableIfNeeded(event.getServer());
    }
}
