package net.itsthesky.upfall.gametest.framework;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import net.itsthesky.upfall.Upfall;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.gamerules.GameRuleMap;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.GameTestHooks;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class UpfallGameTestRegistrar {
    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS = DeferredRegister.create(Registries.TEST_FUNCTION, Upfall.MODID);

    private UpfallGameTestRegistrar() {}

    public static void register(IEventBus modBus) {
        if (!GameTestHooks.isGametestEnabled()) return;
        for (UpfallGameTestClasses.Spec spec : UpfallGameTestClasses.specs()) {
            FUNCTIONS.register(spec.name(), () -> spec.body());
        }
        FUNCTIONS.register(modBus);
        modBus.addListener(UpfallGameTestRegistrar::onRegisterGameTests);
    }

    private static void onRegisterGameTests(RegisterGameTestsEvent event) {
        Map<String, Holder<TestEnvironmentDefinition<?>>> environments = new HashMap<>();
        for (UpfallGameTestClasses.Spec spec : UpfallGameTestClasses.specs()) {
            Holder<TestEnvironmentDefinition<?>> environment = environments.computeIfAbsent(spec.batch(), batch -> event.registerEnvironment(Upfall.id(batch.toLowerCase(Locale.ROOT)),
                    new TestEnvironmentDefinition.SetGameRules(new GameRuleMap.Builder().set(GameRules.RANDOM_TICK_SPEED, 0)
                            .set(GameRules.FIRE_SPREAD_RADIUS_AROUND_PLAYER, 0).build())));
            Identifier id = Upfall.id(spec.name());
            TestData<Holder<TestEnvironmentDefinition<?>>> data = new TestData<>(
                    environment,
                    Upfall.id(spec.template()),
                    spec.timeoutTicks(),
                    spec.setupTicks(),
                    spec.required(),
                    Rotation.NONE,
                    false,
                    1,
                    1,
                    spec.skyAccess(),
                    0);
            event.registerTest(id, new FunctionGameTestInstance(ResourceKey.create(Registries.TEST_FUNCTION, id), data));
        }
    }
}
