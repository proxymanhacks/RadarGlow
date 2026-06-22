package net.radarglow;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Singleton state container for RadarGlow.
 *
 * When the player activates the keybind:
 *   - All LivingEntities within RADIUS blocks are captured into a fixed set.
 *   - A countdown of HIGHLIGHT_TICKS ticks (10 seconds × 20 t/s = 200 ticks) begins.
 *   - While active, those entities return GLOW_COLOR from getTeamColor(), which drives
 *     Minecraft's built-in glowing outline renderer (the same system used by the
 *     Spectral Arrow / Glowing status effect).
 *   - The set is frozen at activation; new entities entering range are NOT added.
 *   - Pressing the keybind while the effect is active does nothing (timer is not reset).
 */
public final class RadarGlowState {

    // ── Constants ────────────────────────────────────────────────────────────

    /** Detection radius in blocks. */
    public static final double RADIUS = 20.0;

    /** How long the highlight lasts (ticks). 200 = 10 seconds. */
    public static final int HIGHLIGHT_TICKS = 200;

    /**
     * Deep blue ARGB color injected as the "team color."
     * Alpha must be 0xFF so Minecraft treats it as a valid color.
     * RGB 0x1A 0x6E 0xFF  →  a vivid cobalt / spectral blue.
     */
    public static final int GLOW_COLOR = 0xFF1A6EFF;

    // ── Singleton ────────────────────────────────────────────────────────────

    private static final RadarGlowState INSTANCE = new RadarGlowState();

    public static RadarGlowState getInstance() {
        return INSTANCE;
    }

    private RadarGlowState() {}

    // ── State ────────────────────────────────────────────────────────────────

    /**
     * The set of entities currently being highlighted.
     * Uses WeakHashMap so dead/unloaded entities are garbage-collected
     * and don't cause memory leaks — we never need to remove them manually.
     */
    private final Set<Entity> highlightedEntities =
            Collections.newSetFromMap(new WeakHashMap<>());

    /** Ticks remaining for the highlight. 0 means inactive. */
    private int ticksRemaining = 0;

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * Called every client tick. Counts down the timer and clears state when done.
     */
    public void tick() {
        if (ticksRemaining > 0) {
            ticksRemaining--;
            if (ticksRemaining == 0) {
                highlightedEntities.clear();
            }
        }
    }

    /**
     * Attempt to activate the highlight.
     * Does nothing if the highlight is already active.
     */
    public void tryActivate() {
        if (ticksRemaining > 0) {
            // Timer is still running — do nothing (as requested)
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        double cx = client.player.getX();
        double cy = client.player.getY();
        double cz = client.player.getZ();

        // Collect all LivingEntities within RADIUS blocks of the player
        // at the moment the key is pressed. The set is frozen from this point.
        highlightedEntities.clear();
        for (Entity entity : client.world.getEntities()) {
            if (entity instanceof LivingEntity && entity != client.player) {
                double dx = entity.getX() - cx;
                double dy = entity.getY() - cy;
                double dz = entity.getZ() - cz;
                if (dx * dx + dy * dy + dz * dz <= RADIUS * RADIUS) {
                    highlightedEntities.add(entity);
                }
            }
        }

        if (!highlightedEntities.isEmpty()) {
            ticksRemaining = HIGHLIGHT_TICKS;
        }
    }

    /**
     * Returns true if this entity is currently being highlighted.
     * Called from the Entity mixin on every render frame.
     */
    public boolean isHighlighted(Entity entity) {
        return ticksRemaining > 0 && highlightedEntities.contains(entity);
    }

    /** Whether the highlight effect is currently active at all. */
    public boolean isActive() {
        return ticksRemaining > 0;
    }

    /** Remaining ticks, for HUD display if desired. */
    public int getTicksRemaining() {
        return ticksRemaining;
    }
}
