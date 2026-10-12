package net.itsthesky.upfall.content.quantum.vault;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.itsthesky.upfall.UpfallConfig;
import net.itsthesky.upfall.content.quantum.QuantumStructures;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacementType;

public class ConfiguredSpreadPlacement extends RandomSpreadStructurePlacement {
    public static final MapCodec<ConfiguredSpreadPlacement> CODEC = Codec.INT.fieldOf("salt")
            .xmap(ConfiguredSpreadPlacement::new, ConfiguredSpreadPlacement::salt);

    public ConfiguredSpreadPlacement(int salt) {
        super(configSpacing(), configSeparation(), RandomSpreadType.LINEAR, salt);
    }

    private static int configSpacing() {
        return UpfallConfig.getInt(UpfallConfig.VAULT_SPACING);
    }

    private static int configSeparation() {
        return Math.min(UpfallConfig.getInt(UpfallConfig.VAULT_SEPARATION), configSpacing() - 1);
    }

    @Override
    public int salt() {
        return super.salt();
    }

    @Override
    public StructurePlacementType<?> type() {
        return QuantumStructures.CONFIGURED_SPREAD.get();
    }
}
