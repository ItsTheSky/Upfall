package net.itsthesky.upfall.compat.mc;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

public final class McEnv {
    private McEnv() {}

    public static Dist dist() {
        //? if >=26.1 {
        /*return FMLEnvironment.getDist();
        *///?} else {
        return FMLEnvironment.dist;
        //?}
    }

    public static boolean isClient() {
        return dist().isClient();
    }
}
