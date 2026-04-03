# 🧟‍♂️ Undead Riders

**Undead Riders** is a Fabric mod for Minecraft that adds naturally spawning undead horsemen to the overworld surface. Six types in total, each tied to a fitting biome. Fully configurable and difficulty-scaled.

[![GitHub Release](https://img.shields.io/github/v/release/SwordfishBE/UndeadRiders?display_name=release&logo=github)](https://github.com/SwordfishBE/UndeadRiders/releases)
[![GitHub Downloads](https://img.shields.io/github/downloads/SwordfishBE/UndeadRiders/total?logo=github)](https://github.com/SwordfishBE/UndeadRiders/releases)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/JVLPDceE?logo=modrinth&logoColor=white&label=Modrinth%20downloads)](https://modrinth.com/mod/undeadriders)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1489518?logo=curseforge&logoColor=white&label=CurseForge%20downloads)](https://www.curseforge.com/minecraft/mc-mods/leashed-teleport)

---

## ✨ Features

- 🐴 **Zombie Horseman** — Zombie on a Zombie Horse. All biomes except Desert. Night only.
- 🏜️ **Husk Horseman** — Husk on a Zombie Horse. Desert only. Night only.
- 💀 **Skeleton Horseman** — Skeleton on a Skeleton Horse. All biomes except Desert/Swamp/Frozen. Night only.
- ☀️ **Parched Horseman** — Parched on a Skeleton Horse. Desert only. Day and night.
- 🌿 **Bogged Horseman** — Bogged on a Skeleton Horse. Swamp and Mangrove Swamp only. Night only.
- ❄️ **Stray Horseman** — Stray on a Skeleton Horse. All frozen biomes. Night only.
- 🎁 **Saddle drop** — Horses only spawn with a saddle at a low configurable rate (default 15% zombie, 30% skeleton). Saddles are looting-aware: Looting III gives ~20% drop chance. Players must hunt at night for saddled horses!
- 🛡️ **Horse armor** — ZombieHorses have a configurable chance to wear armor (default 30%).
- 🔑 **Tameable Skeleton Horses** — After killing the rider, Skeleton Horses can immediately be saddled and ridden.
- 🛡️ **Shield on Hard** — Zombie and Husk riders have a 40% chance to carry a shield.
- 🏹 **Enchanted bow on Hard** — Skeleton, Parched and Stray riders have a 30% chance for a Power I–III bow.
- ⚠️ **Unarmed on Easy** — Zombie and Husk riders have a 25% chance to spawn without a weapon.
- ⚔️ **Difficulty-scaled weapons** — Zombie/Husk riders carry tier-appropriate weapons including spears introduced in modern Minecraft versions.
- 🔀 **Independent toggles** — Each type can be enabled/disabled separately.
- ⚙️ **Fully configurable** — Spawn rates, saddle/armor chances, distances, timing, and more.
- 🔄 **Live reload** — `/undeadriders reload` applies config changes without a restart.
- 📋 **Info command** — `/undeadriders info` shows the current status of all types in-game.
- 🚫 **Non-interfering** — Never touches vanilla's spawn system.

---

## 📊 Difficulty Scaling

### Zombie / Husk Horseman — horse armor

| Difficulty | Option A            | Option B           | Option C            |
|------------|---------------------|--------------------|---------------------|
| Easy       | Leather Horse Armor | Copper Horse Armor | Iron Horse Armor    |
| Normal     | Copper Horse Armor  | Iron Horse Armor   | Gold Horse Armor    |
| Hard       | Iron Horse Armor    | Gold Horse Armor   | Diamond Horse Armor |

Armor only spawns at the configured probability (`zombieHorseArmorChance`, default 30%).

### Zombie / Husk Horseman — rider weapon

| Difficulty | Options (1 chosen at random) |
|------------|-------------------------------|
| Easy       | **25% chance: no weapon.** Otherwise: Stone Sword, Stone Axe, Stone Spear, Wooden Sword, Wooden Axe, Wooden Spear, Wooden Shovel, Stone Shovel |
| Normal     | Copper Sword, Copper Axe, Copper Spear, Iron Sword, Iron Axe, Iron Spear, Golden Sword, Golden Axe, Golden Spear, Iron/Copper/Golden Shovel |
| Hard       | Iron Sword, Iron Axe, Iron Spear, Diamond Sword, Diamond Axe, Diamond Spear, Diamond Shovel |

Drop chance: 5%. On **Hard**: 40% chance of a shield in offhand (no drop).

### Skeleton / Parched / Stray Horseman

Rider equipment handled by vanilla's `finalizeSpawn()`. On **Hard**: 30% chance for a **Power I–III enchanted bow** (drop chance 10%).

---

### Biome restrictions

| Type | Biomes |
|------|--------|
| Zombie Horseman | All overworld biomes **except** Desert, Frozen Ocean, Deep Frozen Ocean, Snowy Beach |
| Husk Horseman | Desert |
| Skeleton Horseman | All overworld biomes **except** Desert, Swamp, Mangrove Swamp, and all frozen biomes |
| Parched Horseman | Desert |
| Bogged Horseman | Swamp, Mangrove Swamp |
| Stray Horseman | Ice Spikes, Snowy Plains, Jagged Peaks, Frozen Peaks, Snowy Slopes, Frozen Ocean, Deep Frozen Ocean, Snowy Beach |

---

## ⚙️ Configuration

Generated automatically on first launch at:
```
<game>/config/undeadriders.json
```

| Option                        | Default | Description |
|-------------------------------|---------|-------------|
| `zombieHorsemanEnabled`       | `true`  | Enable Zombie Horseman |
| `huskHorsemanEnabled`         | `true`  | Enable Husk Horseman (desert) |
| `skeletonHorsemanEnabled`     | `true`  | Enable Skeleton Horseman |
| `parchedHorsemanEnabled`      | `true`  | Enable Parched Horseman (desert) |
| `boggedHorsemanEnabled`       | `true`  | Enable Bogged Horseman (swamp) |
| `strayHorsemanEnabled`        | `true`  | Enable Stray Horseman (frozen biomes) |
| `zombieHorsemanSpawnRate`     | `0.15`  | Spawn chance per attempt, `0.0`–`1.0` |
| `huskHorsemanSpawnRate`       | `0.15`  | Spawn chance per attempt, `0.0`–`1.0` |
| `skeletonHorsemanSpawnRate`   | `0.10`  | Spawn chance per attempt, `0.0`–`1.0` |
| `parchedHorsemanSpawnRate`    | `0.10`  | Spawn chance per attempt, `0.0`–`1.0` |
| `boggedHorsemanSpawnRate`     | `0.15`  | Spawn chance per attempt, `0.0`–`1.0` |
| `strayHorsemanSpawnRate`      | `0.10`  | Spawn chance per attempt, `0.0`–`1.0` |
| `zombieHorseSaddleChance`     | `0.15`  | Chance a ZombieHorse spawns with a saddle. `0.0` = never |
| `skeletonHorseSaddleChance`   | `0.30`  | Chance a SkeletonHorse spawns with a saddle. `0.0` = never |
| `zombieHorseArmorChance`      | `0.30`  | Chance a ZombieHorse spawns wearing armor. `0.0` = never |
| `spawnCheckIntervalTicks`     | `400`   | Ticks between spawn rounds (20 = 1 s) |
| `spawnAttemptsPerPlayer`      | `6`     | Spawn attempts per player per round |
| `minSpawnDistance`            | `24`    | Minimum spawn distance from player (blocks) |
| `maxSpawnDistance`            | `64`    | Maximum spawn distance from player (blocks) |
| `nightOnly`                   | `true`  | Night/thunderstorm only (ignored for Parched) |
| `maxHorsemenPerPlayer`        | `5`     | Max per type per online player |

> ⚠️ **After updating the mod**, delete `config/undeadriders.json` once to regenerate it with any new fields.

### Example config
```jsonc
{
  "zombieHorsemanEnabled": true,   // Enable Zombie Horseman.
  "huskHorsemanEnabled": true,     // Enable Husk Horseman. Desert biomes only.
  "skeletonHorsemanEnabled": true, // Enable Skeleton Horseman.
  "parchedHorsemanEnabled": true,  // Enable Parched Horseman. Desert biomes only.
  "boggedHorsemanEnabled": true,   // Enable Bogged Horseman. Swamp and Mangrove Swamp only.
  "strayHorsemanEnabled": true,    // Enable Stray Horseman. Frozen biomes only.

  "zombieHorsemanSpawnRate": 0.15,   // Zombie Horseman spawn chance per attempt.
  "huskHorsemanSpawnRate": 0.15,     // Husk Horseman spawn chance per attempt.
  "skeletonHorsemanSpawnRate": 0.1,  // Skeleton Horseman spawn chance per attempt.
  "parchedHorsemanSpawnRate": 0.1,   // Parched Horseman spawn chance per attempt.
  "boggedHorsemanSpawnRate": 0.15,   // Bogged Horseman spawn chance per attempt.
  "strayHorsemanSpawnRate": 0.1,     // Stray Horseman spawn chance per attempt.

  "zombieHorseSaddleChance": 0.15,   // Chance a Zombie Horse spawns with a saddle.
  "skeletonHorseSaddleChance": 0.3,  // Chance a Skeleton Horse spawns with a saddle.
  "zombieHorseArmorChance": 0.3,     // Chance a Zombie Horse spawns wearing horse armor.

  "spawnCheckIntervalTicks": 400, // Ticks between spawn rounds. 20 ticks = 1 second.
  "spawnAttemptsPerPlayer": 6,    // Spawn attempts per player during each spawn round.
  "minSpawnDistance": 24,         // Minimum spawn distance from the player.
  "maxSpawnDistance": 64,         // Maximum spawn distance from the player.
  "nightOnly": true,              // Most horsemen only spawn at night or during thunderstorms.
  "maxHorsemenPerPlayer": 5
}
```

---

## 🔄 Commands

Both commands require **gamemaster** permission (op level 2).

| Command | Description |
|---------|-------------|
| `/undeadriders reload` | Reloads the config from disk without a server restart |
| `/undeadriders info` | Shows enabled/disabled status, spawn rates, and current caps in chat |

---

## 🛠️ Technical Details

- **Mod Loader**: [Fabric](https://fabricmc.net/)
- **Required**: [Fabric API](https://modrinth.com/mod/fabric-api)
- **Server-side**: Yes — no client mod required
- **Single player**: Yes — the mod works in single player worlds

---

## 📦 Installation

| Platform   | Link |
|------------|------|
| GitHub     | [Releases](https://github.com/SwordfishBE/UndeadRiders/releases) |
| Modrinth   | [Undead Riders](https://modrinth.com/mod/undeadriders) |
| CurseForge | [Undead Riders](https://www.curseforge.com/minecraft/mc-mods/undead-riders) |

1. Download the latest JAR from your preferred platform above.
2. Place the JAR in your server's `mods/` folder.
3. Make sure [Fabric API](https://modrinth.com/mod/fabric-api) is also installed.
4. Start Minecraft — the config file will be created automatically.

---

## 🧱 Building from Source

```bash
git clone https://github.com/SwordfishBE/UndeadRiders.git
cd UndeadRiders
chmod +x gradlew
./gradlew build
# Output: build/libs/undead-riders-<version>.jar
```

---

## 📄 License

Released under the [AGPL-3.0 License](LICENSE).
