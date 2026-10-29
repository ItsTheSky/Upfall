package net.itsthesky.upfall.gametest.framework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import net.itsthesky.upfall.gametest.AerieGameTests;
import net.itsthesky.upfall.gametest.ChronospireGameTests;
import net.itsthesky.upfall.gametest.ClockworkGameTests;
import net.itsthesky.upfall.gametest.EnchantingGameTests;
import net.itsthesky.upfall.gametest.EngineGameTests;
import net.itsthesky.upfall.gametest.GasCaveGameTests;
import net.itsthesky.upfall.gametest.GearGameTests;
import net.itsthesky.upfall.gametest.GravityGameTests;
import net.itsthesky.upfall.gametest.GrottoGameTests;
import net.itsthesky.upfall.gametest.HaloPortalGameTests;
import net.itsthesky.upfall.gametest.HamletGameTests;
import net.itsthesky.upfall.gametest.IronbarkGameTests;
import net.itsthesky.upfall.gametest.LootGameTests;
import net.itsthesky.upfall.gametest.MachinesGameTests;
import net.itsthesky.upfall.gametest.MobsGameTests;
import net.itsthesky.upfall.gametest.PipeBridgeGameTests;
import net.itsthesky.upfall.gametest.PneumaticPipeGameTests;
import net.itsthesky.upfall.gametest.QuantumGameTests;
import net.itsthesky.upfall.gametest.QuantumUpgradeGameTests;
import net.itsthesky.upfall.gametest.ReliquaryGameTests;
import net.itsthesky.upfall.gametest.RustworksAutomatonGameTests;
import net.itsthesky.upfall.gametest.RustworksGameTests;
import net.itsthesky.upfall.gametest.RustworksWorldgenGameTests;
import net.itsthesky.upfall.gametest.UpfallGameTests;
import net.itsthesky.upfall.gametest.WorldGameTests;
import net.minecraft.gametest.framework.GameTestHelper;

public final class UpfallGameTestClasses {
    public static final List<Class<?>> ALL = List.of(
            AerieGameTests.class,
            ChronospireGameTests.class,
            ClockworkGameTests.class,
            EnchantingGameTests.class,
            EngineGameTests.class,
            GasCaveGameTests.class,
            GearGameTests.class,
            GravityGameTests.class,
            GrottoGameTests.class,
            HaloPortalGameTests.class,
            HamletGameTests.class,
            IronbarkGameTests.class,
            LootGameTests.class,
            MachinesGameTests.class,
            MobsGameTests.class,
            PipeBridgeGameTests.class,
            PneumaticPipeGameTests.class,
            QuantumGameTests.class,
            QuantumUpgradeGameTests.class,
            ReliquaryGameTests.class,
            RustworksAutomatonGameTests.class,
            RustworksGameTests.class,
            RustworksWorldgenGameTests.class,
            UpfallGameTests.class,
            WorldGameTests.class
    );

    public record Spec(
            String name,
            String template,
            int timeoutTicks,
            String batch,
            int setupTicks,
            boolean required,
            boolean skyAccess,
            Consumer<GameTestHelper> body) {}

    private UpfallGameTestClasses() {}

    public static List<Spec> specs() {
        List<Spec> specs = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (Class<?> type : ALL) {
            List<Method> methods = new ArrayList<>(List.of(type.getDeclaredMethods()));
            methods.sort(Comparator.comparing(Method::getName));
            for (Method method : methods) {
                UpfallGameTest test = method.getAnnotation(UpfallGameTest.class);
                if (test == null) continue;
                String name = method.getName().toLowerCase(Locale.ROOT);
                if (!names.add(name)) throw new IllegalStateException("Duplicate game test name " + name);
                specs.add(new Spec(name, test.template(), test.timeoutTicks(), test.batch(), test.setupTicks(), test.required(), test.skyAccess(), invoker(method)));
            }
        }
        return specs;
    }

    private static Consumer<GameTestHelper> invoker(Method method) {
        return helper -> {
            try {
                method.invoke(null, helper);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException runtime) throw runtime;
                throw new RuntimeException(e.getCause());
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
