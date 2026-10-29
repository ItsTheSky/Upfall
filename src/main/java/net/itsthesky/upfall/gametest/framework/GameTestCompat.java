package net.itsthesky.upfall.gametest.framework;

import net.itsthesky.upfall.compat.mc.McLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class GameTestCompat {
    private GameTestCompat() {}

    public static <T extends BlockEntity> T blockEntity(GameTestHelper helper, BlockPos pos, Class<T> type) {
        //? if >=26.1 {
        /*return helper.getBlockEntity(pos, type);
        *///?} else {
        BlockEntity blockEntity = helper.getBlockEntity(pos);
        return type.cast(blockEntity);
        //?}
    }

    public static RuntimeException failure(GameTestHelper helper, String message) {
        //? if >=26.1 {
        /*return helper.assertionException(net.minecraft.network.chat.Component.literal(message));
        *///?} else {
        return new GameTestAssertException(message);
        //?}
    }

    public static long dayTime(GameTestHelper helper) {
        return McLevel.dayTime(helper.getLevel());
    }

    public static void setDayTime(GameTestHelper helper, long time) {
        //? if >=26.1 {
        /*helper.setTime(time);
        *///?} else {
        helper.getLevel().setDayTime(time);
        //?}
    }

    public static void setDaylightCycle(GameTestHelper helper, boolean enabled) {
        ServerLevel level = helper.getLevel();
        //? if >=26.1 {
        /*level.getGameRules().set(GameRules.ADVANCE_TIME, enabled, level.getServer());
        *///?} else {
        level.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(enabled, level.getServer());
        //?}
    }
}
