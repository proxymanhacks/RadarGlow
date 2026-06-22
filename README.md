# RadarGlow

A client-side Fabric mod for Minecraft 1.21.x.

Press **G** (rebindable in Options → Controls → RadarGlow) to highlight all living entities
(mobs and players) within a 20-block radius with a glowing deep-blue outline for **10 seconds**.

- The highlight is frozen at the moment you press the key — new mobs that enter range are not added.
- Pressing the key while the effect is active does nothing; wait for it to expire.
- Works on servers that don't have the mod installed (purely client-side).

## Building

**Requirements:** JDK 21, internet connection (for Gradle to download dependencies on first build)

```bash
# On Linux/macOS
./gradlew build

# On Windows
gradlew.bat build
```

The compiled `.jar` will be at:
```
build/libs/radarglow-1.0.0.jar
```

Drop it into your `.minecraft/mods/` folder alongside Fabric API.

## Version Note

This mod targets Minecraft **1.21.1** by default. If you are on **1.21.11**:

1. Open `gradle.properties`
2. Change `minecraft_version` to `1.21.11`
3. Change `yarn_mappings` to the correct Yarn build for 1.21.11 (check https://fabricmc.net/develop/)
4. Update `fabric_version` and `loader_version` to match
5. Run `./gradlew build`

## How It Works

Uses a Mixin on `Entity#getTeamColorValue()` and `Entity#isGlowing()` —
the same technique as re:entity outliner — to make the vanilla glowing outline renderer
draw a deep-blue outline around targeted entities.
