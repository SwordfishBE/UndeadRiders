package net.undeadriders.spawn;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FrostbiteBiomeTest {
    private Holder<Biome> biome(ResourceKey<Biome> key) {
        return Holder.Reference.createStandAlone(new HolderOwner<Biome>() { }, key);
    }

    @Test
    void acceptsTheEightAgreedColdSurfaceBiomes() {
        for (var key : List.of(Biomes.ICE_SPIKES, Biomes.SNOWY_PLAINS, Biomes.JAGGED_PEAKS,
                Biomes.FROZEN_PEAKS, Biomes.SNOWY_SLOPES, Biomes.SNOWY_BEACH, Biomes.SNOWY_TAIGA, Biomes.GROVE)) {
            assertTrue(UndeadHorsemanSpawner.isFrostbiteBiome(biome(key)), key.toString());
        }
    }

    @Test
    void excludesRiversOceansCavesAndOtherBiomes() {
        for (var key : List.of(Biomes.FROZEN_RIVER, Biomes.RIVER, Biomes.FROZEN_OCEAN,
                Biomes.DEEP_FROZEN_OCEAN, Biomes.COLD_OCEAN, Biomes.DEEP_COLD_OCEAN,
                Biomes.ICE_CAVES, Biomes.PLAINS, Biomes.TAIGA, Biomes.DESERT, Biomes.MUSHROOM_FIELDS)) {
            assertFalse(UndeadHorsemanSpawner.isFrostbiteBiome(biome(key)), key.toString());
        }
    }
}
