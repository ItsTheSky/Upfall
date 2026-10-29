package net.itsthesky.upfall.gametest.framework;

import java.util.Collection;
import net.itsthesky.upfall.Upfall;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.gametest.GameTestHolder;

@GameTestHolder(Upfall.MODID)
public final class UpfallGameTestRegistrar {
    private UpfallGameTestRegistrar() {}

    public static void register(IEventBus modBus) {}

    @GameTestGenerator
    public static Collection<TestFunction> generate() {
        return UpfallGameTestClasses.specs().stream()
                .map(spec -> new TestFunction(
                        spec.batch(),
                        spec.name(),
                        Upfall.MODID + ":" + spec.template(),
                        Rotation.NONE,
                        spec.timeoutTicks(),
                        spec.setupTicks(),
                        spec.required(),
                        false,
                        1,
                        1,
                        spec.skyAccess(),
                        spec.body()))
                .toList();
    }
}
