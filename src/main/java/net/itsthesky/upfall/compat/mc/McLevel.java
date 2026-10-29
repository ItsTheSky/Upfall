package net.itsthesky.upfall.compat.mc;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelHeightAccessor;

public final class McLevel {
    private McLevel() {}

    public static int minY(LevelHeightAccessor level) {
        //? if >=26.1 {
        /*return level.getMinY();
        *///?} else {
        return level.getMinBuildHeight();
        //?}
    }

    public static int maxYExclusive(LevelHeightAccessor level) {
        //? if >=26.1 {
        /*return level.getMaxY() + 1;
        *///?} else {
        return level.getMaxBuildHeight();
        //?}
    }

    public static long dayTime(Level level) {
        //? if >=26.1 {
        /*return level.getOverworldClockTime();
        *///?} else {
        return level.getDayTime();
        //?}
    }

    public static boolean daylightCycle(Level level) {
        //? if >=26.1 {
        /*return level instanceof ServerLevel serverLevel
                ? serverLevel.getGameRules().get(GameRules.ADVANCE_TIME)
                : DaylightCycleSync.clientValue();
        *///?} else {
        return level.getGameRules().getBoolean(GameRules.RULE_DAYLIGHT);
        //?}
    }

    public static MinecraftServer server(Entity entity) {
        //? if >=26.1 {
        /*return entity.level().getServer();
        *///?} else {
        return entity.getServer();
        //?}
    }

    public static ServerLevel serverLevel(ServerPlayer player) {
        //? if >=26.1 {
        /*return player.level();
        *///?} else {
        return player.serverLevel();
        //?}
    }

    public static RecipeManager serverRecipes(ServerLevel level) {
        //? if >=26.1 {
        /*return level.recipeAccess();
        *///?} else {
        return level.getRecipeManager();
        //?}
    }
}
