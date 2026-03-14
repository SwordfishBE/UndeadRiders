# 🧟‍♂️ Undead Riders

**Undead Riders** is a Fabric mod for Minecraft that adds naturally spawning undead horsemen to the overworld surface. Five types in total, each tied to a fitting biome. Fully configurable and difficulty-scaled.

---

## ✨ Features

- 🐴 **Zombie Horseman** — Zombie riding a Zombie Horse with difficulty-scaled armor and weapon. Spawns in all overworld biomes at night.
- 💀 **Skeleton Horseman** — Skeleton riding a wild Skeleton Horse. Bow + possible Power enchant on Hard.
- ☀️ **Parched Horseman** — Parched (desert skeleton variant) on a Skeleton Horse. **Desert biomes only.** Spawns day and night — Parched don't burn in sunlight.
- 🌿 **Bogged Horseman** — Bogged on a Skeleton Horse. **Swamp and Mangrove Swamp only.** Night only.
- ❄️ **Stray Horseman** — Stray on a Skeleton Horse. **Frozen biomes only** (Ice Spikes, Snowy Plains, Jagged Peaks, Frozen Peaks, Snowy Slopes). Night only.
- ⚔️ **Weapons** — Zombie riders can carry Wooden, Stone, Iron, or Diamond Weapons.
- 🛡️ **Difficulty-scaled horse armor** — Zombie Horses wear armor that scales with game difficulty.
- 🔀 **Independent toggles** — Each horseman type can be enabled/disabled separately.
- ⚙️ **Fully configurable** — Spawn rates, distances, timing, attempts per player, night-only toggle via JSON config.
- 🔄 **Live reload** — `/undeadriders reload` applies config changes instantly without a restart.
- 🚫 **Non-interfering** — Never touches vanilla's spawn system.

---

## 📊 Difficulty Scaling

### Zombie Horseman — horse armor

| Difficulty | Option A            | Option B           | Option C            |
|------------|---------------------|--------------------|---------------------|
| Easy       | Leather Horse Armor | Copper Horse Armor | Iron Horse Armor    |
| Normal     | Copper Horse Armor  | Iron Horse Armor   | Gold Horse Armor    |
| Hard       | Iron Horse Armor    | Gold Horse Armor   | Diamond Horse Armor |

One option chosen at random.

### Zombie Horseman — rider weapon

| Difficulty | Options (1 chosen at random) |
|------------|-------------------------------|
| Easy       | Stone Sword, Stone Axe, Stone Spear, Wooden Sword, Wooden Axe, Wooden Spear, Wooden Shovel, Stone Shovel |
| Normal     | Copper Sword, Copper Axe, Copper Spear, Iron Sword, Iron Axe, Iron Spear, Golden Sword, Golden Axe, Golden Spear, Iron/Copper/Golden Shovel |
| Hard       | Iron Sword, Iron Axe, Iron Spear, Diamond Sword, Diamond Axe, Diamond Spear, Diamond Shovel |

One option chosen at random. Drop chance: 5%.

### Skeleton / Parched / Bogged / Stray Horseman

Rider equipment handled entirely by vanilla's `finalizeSpawn()` — bow with possible Power enchant on Hard, consistent with their normal spawn behaviour.

---

### Biome restrictions

| Type | Biomes |
|------|--------|
| Zombie Horseman | All overworld biomes |
| Skeleton Horseman | All overworld biomes **except** Desert, Swamp, Mangrove Swamp, Ice Spikes, Snowy Plains, Jagged Peaks, Frozen Peaks, Snowy Slopes |
| Parched Horseman | Desert |
| Bogged Horseman | Swamp, Mangrove Swamp |
| Stray Horseman | Ice Spikes, Snowy Plains, Jagged Peaks, Frozen Peaks, Snowy Slopes |

---

## ⚙️ Configuration

Generated automatically on first launch at:
```
<game>/config/undeadriders.json
```

| Option                        | Default | Description |
|-------------------------------|---------|-------------|
| `zombieHorsemanEnabled`       | `true`  | Enable Zombie Horseman |
| `skeletonHorsemanEnabled`     | `true`  | Enable Skeleton Horseman |
| `parchedHorsemanEnabled`      | `true`  | Enable Parched Horseman (desert) |
| `boggedHorsemanEnabled`       | `true`  | Enable Bogged Horseman (swamp) |
| `strayHorsemanEnabled`        | `true`  | Enable Stray Horseman (frozen biomes) |
| `zombieHorsemanSpawnRate`     | `0.15`  | Spawn chance per attempt, `0.0`–`1.0` |
| `skeletonHorsemanSpawnRate`   | `0.15`  | Spawn chance per attempt, `0.0`–`1.0` |
| `parchedHorsemanSpawnRate`    | `0.15`  | Spawn chance per attempt, `0.0`–`1.0` |
| `boggedHorsemanSpawnRate`     | `0.15`  | Spawn chance per attempt, `0.0`–`1.0` |
| `strayHorsemanSpawnRate`      | `0.15`  | Spawn chance per attempt, `0.0`–`1.0` |
| `spawnCheckIntervalTicks`     | `400`   | Ticks between spawn rounds (20 = 1 s) |
| `spawnAttemptsPerPlayer`      | `6`     | Spawn attempts per player per round — raise this to see more horsemen |
| `minSpawnDistance`            | `24`    | Minimum spawn distance from player (blocks) |
| `maxSpawnDistance`            | `64`    | Maximum spawn distance from player (blocks) |
| `nightOnly`                   | `true`  | Night/thunderstorm only (ignored for Parched) |
| `maxHorsemenPerPlayer`        | `5`     | Max per type per online player |

> ⚠️ **After updating the mod**, delete `config/undeadriders.json` once to regenerate it with any new fields.

### Example config
```json
{
  "zombieHorsemanEnabled": true,
  "skeletonHorsemanEnabled": true,
  "parchedHorsemanEnabled": true,
  "boggedHorsemanEnabled": true,
  "strayHorsemanEnabled": true,
  "zombieHorsemanSpawnRate": 0.15,
  "skeletonHorsemanSpawnRate": 0.15,
  "parchedHorsemanSpawnRate": 0.15,
  "boggedHorsemanSpawnRate": 0.15,
  "strayHorsemanSpawnRate": 0.15,
  "spawnCheckIntervalTicks": 400,
  "spawnAttemptsPerPlayer": 6,
  "minSpawnDistance": 24,
  "maxSpawnDistance": 64,
  "nightOnly": true,
  "maxHorsemenPerPlayer": 5
}
```

---

## 🔄 In-game Reload

```
/undeadriders reload
```
Requires **gamemaster** permission level (op level 2). Applies immediately — no restart needed.

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
