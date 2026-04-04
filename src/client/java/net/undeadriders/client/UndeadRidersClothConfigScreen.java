package net.undeadriders.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.undeadriders.UndeadRiders;
import net.undeadriders.config.UndeadRidersConfig;

final class UndeadRidersClothConfigScreen {
    private UndeadRidersClothConfigScreen() {
    }

    static Screen create(Screen parent) {
        UndeadRidersConfig config = UndeadRiders.loadConfigForEditing();

        ConfigBuilder builder = ConfigBuilder.create()
            .setParentScreen(parent)
            .setTitle(Component.literal("Undead Riders Config"))
            .setSavingRunnable(() -> UndeadRiders.applyEditedConfig(config));

        ConfigEntryBuilder entries = builder.entryBuilder();

        ConfigCategory horsemen = builder.getOrCreateCategory(Component.literal("Horsemen"));
        horsemen.addEntry(entries.startBooleanToggle(Component.literal("Zombie Horseman"), config.zombieHorsemanEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable Zombie Horseman spawns."))
            .setSaveConsumer(value -> config.zombieHorsemanEnabled = value)
            .build());
        horsemen.addEntry(entries.startBooleanToggle(Component.literal("Husk Horseman"), config.huskHorsemanEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable Husk Horseman spawns in desert biomes."))
            .setSaveConsumer(value -> config.huskHorsemanEnabled = value)
            .build());
        horsemen.addEntry(entries.startBooleanToggle(Component.literal("Skeleton Horseman"), config.skeletonHorsemanEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable Skeleton Horseman spawns."))
            .setSaveConsumer(value -> config.skeletonHorsemanEnabled = value)
            .build());
        horsemen.addEntry(entries.startBooleanToggle(Component.literal("Parched Horseman"), config.parchedHorsemanEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable Parched Horseman spawns in desert biomes."))
            .setSaveConsumer(value -> config.parchedHorsemanEnabled = value)
            .build());
        horsemen.addEntry(entries.startBooleanToggle(Component.literal("Bogged Horseman"), config.boggedHorsemanEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable Bogged Horseman spawns in swamp biomes."))
            .setSaveConsumer(value -> config.boggedHorsemanEnabled = value)
            .build());
        horsemen.addEntry(entries.startBooleanToggle(Component.literal("Stray Horseman"), config.strayHorsemanEnabled)
            .setDefaultValue(true)
            .setTooltip(Component.literal("Enable Stray Horseman spawns in frozen biomes."))
            .setSaveConsumer(value -> config.strayHorsemanEnabled = value)
            .build());

        ConfigCategory spawnRates = builder.getOrCreateCategory(Component.literal("Spawn Rates"));
        spawnRates.addEntry(entries.startFloatField(Component.literal("Zombie Horseman Spawn Rate"), config.zombieHorsemanSpawnRate)
            .setDefaultValue(0.15f)
            .setMin(0.0f)
            .setMax(1.0f)
            .setTooltip(Component.literal("Spawn chance per attempt for Zombie Horsemen."))
            .setSaveConsumer(value -> config.zombieHorsemanSpawnRate = value)
            .build());
        spawnRates.addEntry(entries.startFloatField(Component.literal("Husk Horseman Spawn Rate"), config.huskHorsemanSpawnRate)
            .setDefaultValue(0.15f)
            .setMin(0.0f)
            .setMax(1.0f)
            .setTooltip(Component.literal("Spawn chance per attempt for Husk Horsemen."))
            .setSaveConsumer(value -> config.huskHorsemanSpawnRate = value)
            .build());
        spawnRates.addEntry(entries.startFloatField(Component.literal("Skeleton Horseman Spawn Rate"), config.skeletonHorsemanSpawnRate)
            .setDefaultValue(0.10f)
            .setMin(0.0f)
            .setMax(1.0f)
            .setTooltip(Component.literal("Spawn chance per attempt for Skeleton Horsemen."))
            .setSaveConsumer(value -> config.skeletonHorsemanSpawnRate = value)
            .build());
        spawnRates.addEntry(entries.startFloatField(Component.literal("Parched Horseman Spawn Rate"), config.parchedHorsemanSpawnRate)
            .setDefaultValue(0.10f)
            .setMin(0.0f)
            .setMax(1.0f)
            .setTooltip(Component.literal("Spawn chance per attempt for Parched Horsemen."))
            .setSaveConsumer(value -> config.parchedHorsemanSpawnRate = value)
            .build());
        spawnRates.addEntry(entries.startFloatField(Component.literal("Bogged Horseman Spawn Rate"), config.boggedHorsemanSpawnRate)
            .setDefaultValue(0.15f)
            .setMin(0.0f)
            .setMax(1.0f)
            .setTooltip(Component.literal("Spawn chance per attempt for Bogged Horsemen."))
            .setSaveConsumer(value -> config.boggedHorsemanSpawnRate = value)
            .build());
        spawnRates.addEntry(entries.startFloatField(Component.literal("Stray Horseman Spawn Rate"), config.strayHorsemanSpawnRate)
            .setDefaultValue(0.10f)
            .setMin(0.0f)
            .setMax(1.0f)
            .setTooltip(Component.literal("Spawn chance per attempt for Stray Horsemen."))
            .setSaveConsumer(value -> config.strayHorsemanSpawnRate = value)
            .build());

        ConfigCategory equipment = builder.getOrCreateCategory(Component.literal("Equipment"));
        equipment.addEntry(entries.startFloatField(Component.literal("Zombie Horse Saddle Chance"), config.zombieHorseSaddleChance)
            .setDefaultValue(0.15f)
            .setMin(0.0f)
            .setMax(1.0f)
            .setTooltip(Component.literal("Chance a Zombie Horse spawns with a saddle equipped."))
            .setSaveConsumer(value -> config.zombieHorseSaddleChance = value)
            .build());
        equipment.addEntry(entries.startFloatField(Component.literal("Skeleton Horse Saddle Chance"), config.skeletonHorseSaddleChance)
            .setDefaultValue(0.30f)
            .setMin(0.0f)
            .setMax(1.0f)
            .setTooltip(Component.literal("Chance a Skeleton Horse spawns with a saddle equipped."))
            .setSaveConsumer(value -> config.skeletonHorseSaddleChance = value)
            .build());
        equipment.addEntry(entries.startFloatField(Component.literal("Zombie Horse Armor Chance"), config.zombieHorseArmorChance)
            .setDefaultValue(0.30f)
            .setMin(0.0f)
            .setMax(1.0f)
            .setTooltip(Component.literal("Chance a Zombie Horse spawns wearing horse armor."))
            .setSaveConsumer(value -> config.zombieHorseArmorChance = value)
            .build());

        ConfigCategory spawning = builder.getOrCreateCategory(Component.literal("Spawning"));
        spawning.addEntry(entries.startIntField(Component.literal("Spawn Check Interval Ticks"), config.spawnCheckIntervalTicks)
            .setDefaultValue(400)
            .setMin(20)
            .setTooltip(Component.literal("Ticks between spawn rounds. 20 ticks = 1 second."))
            .setSaveConsumer(value -> config.spawnCheckIntervalTicks = value)
            .build());
        spawning.addEntry(entries.startIntField(Component.literal("Spawn Attempts Per Player"), config.spawnAttemptsPerPlayer)
            .setDefaultValue(6)
            .setMin(1)
            .setTooltip(Component.literal("Spawn attempts per player during each spawn round."))
            .setSaveConsumer(value -> config.spawnAttemptsPerPlayer = value)
            .build());
        spawning.addEntry(entries.startIntField(Component.literal("Minimum Spawn Distance"), config.minSpawnDistance)
            .setDefaultValue(24)
            .setMin(8)
            .setTooltip(Component.literal("Minimum distance from the player in blocks."))
            .setSaveConsumer(value -> config.minSpawnDistance = value)
            .build());
        spawning.addEntry(entries.startIntField(Component.literal("Maximum Spawn Distance"), config.maxSpawnDistance)
            .setDefaultValue(64)
            .setMin(16)
            .setTooltip(Component.literal("Maximum distance from the player in blocks."))
            .setSaveConsumer(value -> config.maxSpawnDistance = value)
            .build());
        spawning.addEntry(entries.startBooleanToggle(Component.literal("Night Only"), config.nightOnly)
            .setDefaultValue(true)
            .setTooltip(Component.literal("If enabled, most horsemen only spawn at night or during thunderstorms."))
            .setSaveConsumer(value -> config.nightOnly = value)
            .build());
        spawning.addEntry(entries.startIntField(Component.literal("Max Horsemen Per Player"), config.maxHorsemenPerPlayer)
            .setDefaultValue(5)
            .setMin(0)
            .setTooltip(Component.literal("Maximum number of horsemen of each type per online player."))
            .setSaveConsumer(value -> config.maxHorsemenPerPlayer = value)
            .build());

        return builder.build();
    }
}
