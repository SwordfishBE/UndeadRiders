# 🧟‍♂️ Undead Riders

**Undead Riders** is a Fabric mod for Minecraft that adds naturally spawning undead horsemen to the overworld surface. Six types in total, each tied to a fitting biome. Fully configurable and difficulty-scaled.

---

## ✨ Features

- 🐴 **Zombie Horseman** — Zombie on a Zombie Horse. All biomes except Desert. Night only.
- 🏜️ **Husk Horseman** — Husk on a Zombie Horse. Desert only. Night only.
- 💀 **Skeleton Horseman** — Skeleton on a Skeleton Horse. All biomes except Desert/Swamp/Frozen. Night only.
- ☀️ **Parched Horseman** — Parched on a Skeleton Horse. Desert only. Day and night.
- 🌿 **Bogged Horseman** — Bogged on a Skeleton Horse. Swamp and Mangrove Swamp only. Night only.
- ❄️ **Stray Horseman** — Stray on a Skeleton Horse. All frozen biomes. Night only.
- 🎁 **Saddle drop** — Horses only spawn with a saddle at a low configurable rate (default 15% zombie, 10% skeleton). Saddles are looting-aware: Looting III gives ~20% drop chance. Players must hunt at night for saddled horses!
- 🛡️ **Horse armor** — ZombieHorses have a configurable chance to wear armor (default 60%). 40% spawn without.
- 🔑 **Tameable Skeleton Horses** — After killing the rider, Skeleton Horses can immediately be saddled and ridden.
- 🛡️ **Shield on Hard** — Zombie and Husk riders have a 40% chance to carry a shield.
- 🏹 **Enchanted bow on Hard** — Skeleton, Parched and Stray riders have a 30% chance for a Power I–III bow.
- ⚠️ **Unarmed on Easy** — Zombie and Husk riders have a 25% chance to spawn without a weapon.
- ⚔️ **Difficulty-scaled weapons** — Zombie/Husk riders carry tier-appropriate weapons including 1.21.11 Spears.
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

Armor only spawns at the configured probability (`zombieHorseArmorChance`, default 60%).

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
```json
{
  "zombieHorsemanEnabled": true,
  "huskHorsemanEnabled": true,
  "skeletonHorsemanEnabled": true,
  "parchedHorsemanEnabled": true,
  "boggedHorsemanEnabled": true,
  "strayHorsemanEnabled": true,
  "zombieHorsemanSpawnRate": 0.15,
  "huskHorsemanSpawnRate": 0.15,
  "skeletonHorsemanSpawnRate": 0.1,
  "parchedHorsemanSpawnRate": 0.1,
  "boggedHorsemanSpawnRate": 0.15,
  "strayHorsemanSpawnRate": 0.1,
  "zombieHorseSaddleChance": 0.15,
  "skeletonHorseSaddleChance": 0.3,
  "zombieHorseArmorChance": 0.3,
  "spawnCheckIntervalTicks": 400,
  "spawnAttemptsPerPlayer": 6,
  "minSpawnDistance": 24,
  "maxSpawnDistance": 64,
  "nightOnly": true,
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
- **Mappings**: Official Mojang Mappings
- **Required**: [Fabric API](https://modrinth.com/mod/fabric-api)
- **Server-side**: Yes — no client mod required
- **Single player**: Yes — the mod works in single player worlds

---

## 📦 Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and place it in `mods/`.
3. Download `undead-riders-<version>.jar` and place it in `mods/`.
4. Launch Minecraft. The config is created automatically on first run.

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

