package net.radarglow;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * RadarGlow — Client Mod Initializer
 *
 * Registers the keybind (default: G) under the "RadarGlow" category
 * in Minecraft's Controls menu, and hooks the client tick to:
 *   1. Advance the highlight timer.
 *   2. Check if the keybind was pressed and activate if so.
 */
public class RadarGlowClient implements ClientModInitializer {

    /** The keybind shown in Options → Controls → RadarGlow */
    public static KeyBinding ACTIVATE_KEY;

    @Override
    public void onInitializeClient() {
        // Register the keybind
        ACTIVATE_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.radarglow.activate",          // translation key
                InputUtil.Type.KEYSYM,              // keyboard key (not mouse)
                GLFW.GLFW_KEY_G,                    // default: G
                "category.radarglow.main"           // Controls category name
        ));

        // Each client tick: advance timer, then check for key press
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            RadarGlowState state = RadarGlowState.getInstance();

            // Tick the countdown timer
            state.tick();

            // Consume all queued presses of the keybind this tick
            while (ACTIVATE_KEY.wasPressed()) {
                state.tryActivate();
            }
        });
    }
}
