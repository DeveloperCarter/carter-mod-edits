# Carter Mod Edits

A small NeoForge mod with personal quality-of-life tweaks for an
[All the Mods 10](https://www.curseforge.com/minecraft/modpacks/all-the-mods-10)
(ATM10) world. It bundles three unrelated features:

1. A server-side countdown **timer** command.
2. A client-side **FOV fix** for the Artifacts "Running Shoes".
3. A client-side **`/noclip`** toggle.

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
the shoes' speed boost is active, resets the FOV back to your configured base
FOV. **You keep the full speed boost; only the camera zoom is removed.**

Instead of looking for the worn item, the patch detects the boost itself: the
shoes apply a transient `MOVEMENT_SPEED` modifier with the id
`artifacts:sprinting_speed` while you sprint. The patch checks for that modifier
on the local player and, when present, overrides the computed FOV.

Detecting the attribute rather than the item means it works no matter which slot
mod equips the shoes. This matters on ATM10: the shoes sit in an **Accessories**
slot, not a Curios slot, so an item lookup through the Curios API never finds
them. The attribute is the same either way, so there is no dependency on Curios,
Accessories, or even Artifacts at compile time.

Implementation: `patch/FovPatchClient` (client-only `@EventBusSubscriber`).

### 3. Noclip toggle (client side)

A `/noclip` command that lets you fly through blocks while staying in your
current game mode (unlike `/gamemode spectator`, which makes you invisible and
non-interactive).

```
/noclip
```

Run it once to enable, again to disable. While enabled you get creative-style
flight and pass through blocks; suffocation is disabled automatically.

Noclip for your own player has to be client-side, because the client computes
its own movement collision, so this is a **client command** (it works from
singleplayer and on LAN). It flips the vanilla `noPhysics` flag and turns on
flight, announcing the flight to the server via `onUpdateAbilities()` so it is
not rejected as illegal flying. On a dedicated server with movement anti-cheat
it may rubber-band or get you kicked. Flight is only removed on disable if this
toggle is what granted it, so it never strips flight from a creative player.

Implementation: `patch/NoclipClient` (client-only `@EventBusSubscriber`).

---

## Dependencies

- **NeoForge** 21.1.x and **Minecraft** 1.21.1 (required).

No other mods are required to build or run. The FOV fix keys off the
`artifacts:sprinting_speed` attribute modifier by id, so if Artifacts is not
installed (or the shoes are not equipped) it simply never triggers.

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
    NoclipClient.java    /noclip toggle
src/main/resources/META-INF/
  neoforge.mods.toml     mod metadata + dependencies
```

> Note: the Java package is still `com.DeveloperCarter.timer` for historical
> reasons (this started life as a timer-only mod). The mod id and display name
> are now `carter_mod` / "Carter Mod Edits".

## License

MIT.
