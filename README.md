# Carter Mod Edits

A small NeoForge mod with personal quality-of-life tweaks for an
[All the Mods 10](https://www.curseforge.com/minecraft/modpacks/all-the-mods-10)
(ATM10) world. It bundles two unrelated features:

1. A server-side countdown **timer** command.
2. A client-side **FOV fix** for the Artifacts "Running Shoes".

| | |
|---|---|
| Minecraft | 1.21.1 |
| Loader | NeoForge 21.1.x |
| Mod id | `carter_mod` |
| Java | 21 |

---

## Features

### 1. Timer command (server side)

A simple per-player countdown timer. The remaining time is saved with the world
(in the Overworld's saved data), so it survives relog and restart. When a timer
finishes the player gets an on-screen message and a short series of note-block
pings.

**Commands**

```
/settimer <number> seconds|minutes|hours [label]
/timer status
/timer cancel
```

- `<number>` must be a positive integer. Max length is 12 hours.
- `[label]` is optional free text shown in the start/status/done messages.
- Each player has one active timer at a time; setting a new one replaces it.

**Examples**

```
/settimer 90 seconds
/settimer 25 minutes pomodoro
/settimer 2 hours base raid
/timer status
/timer cancel
```

Implementation: `TimerCommand` (command tree), `TimerScheduler` (ticks down each
server tick and fires alerts), `TimerData` (`SavedData` persistence).

### 2. Running Shoes FOV fix (client side)

The Artifacts mod's **Running Shoes** grant a sprint movement-speed bonus.
Vanilla Minecraft widens your FOV whenever movement speed goes up, so the shoes
cause a constant zoom-out that many people find nauseating. There is no config
option in Artifacts to turn this off without also removing the speed bonus,
because the FOV change is a vanilla side effect of the speed attribute.

This patch fixes it surgically: it hooks `ViewportEvent.ComputeFov` and, while
you are wearing the Running Shoes, resets the FOV back to your configured base
FOV. **You keep the full speed boost; only the camera zoom is removed.**

Detection covers both ways the shoes can be equipped:

- **Curios slot** (the normal case on ATM10) via the Curios API, and
- a fallback scan of the vanilla armor slots.

The whole feature is guarded so it is a no-op if Artifacts is not loaded, and
the Curios path only runs when Curios is present.

Implementation: `patch/FovPatchClient` (client-only `@EventBusSubscriber`).

---

## Dependencies

- **NeoForge** 21.1.x and **Minecraft** 1.21.1 (required).
- **Curios** 9.x (optional, client). Needed only so the FOV fix can detect
  shoes worn in a Curios slot. If Curios is absent the mod still loads and the
  fix falls back to checking armor slots.

The FOV fix targets the item `artifacts:running_shoes`. If Artifacts is not
installed, that half of the mod simply does nothing.

---

## Building

Requires JDK 21.

```bash
./gradlew build
```

The finished jar lands in `build/libs/carter-mod-edits-<version>.jar`. Drop it
into your instance's `mods/` folder.

---

## Project layout

```
src/main/java/com/DeveloperCarter/timer/
  TimerMod.java          mod entry point, registers the command + scheduler
  TimerCommand.java      /settimer and /timer command tree
  TimerScheduler.java    per-tick countdown + completion alerts
  TimerData.java         world-saved timer state
  patch/
    FovPatchClient.java  Running Shoes FOV fix
src/main/resources/META-INF/
  neoforge.mods.toml     mod metadata + dependencies
```

> Note: the Java package is still `com.DeveloperCarter.timer` for historical
> reasons (this started life as a timer-only mod). The mod id and display name
> are now `carter_mod` / "Carter Mod Edits".

## License

MIT.
