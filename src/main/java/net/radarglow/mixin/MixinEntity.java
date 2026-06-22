package net.radarglow.mixin;

import net.minecraft.entity.Entity;
import net.radarglow.RadarGlowState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * MixinEntity
 *
 * Injects into Entity#getTeamColor() to return a custom deep-blue color
 * for any entity that RadarGlowState has marked as highlighted.
 *
 * HOW THE GLOW WORKS:
 * Minecraft's outline renderer (used by the Glowing status effect and
 * Spectral Arrows) asks each entity for its "team color" to tint the outline.
 * If we return our own color here AND also make isGlowing() return true,
 * the renderer draws the glowing outline in our color around the entity —
 * fully visible through walls, identical to the spectral arrow effect.
 *
 * We hook getTeamColor() (not isGlowing()) because isGlowing() is already
 * intercepted by the vanilla glow system; overriding it can conflict.
 * Instead, we piggyback on the existing glow path:
 *   isGlowing() check → getTeamColor() → outline rendered in that color.
 *
 * To make highlighted entities actually glow, we also need to make the game
 * think they're glowing. We do this via a second @Inject on isGlowing().
 */
@Mixin(Entity.class)
public abstract class MixinEntity {

    /**
     * Override getTeamColor() so the outline is deep blue for highlighted entities.
     * Called every render frame per entity; must be fast.
     */
    @Inject(
            method = "getTeamColorValue",
            at = @At("HEAD"),
            cancellable = true
    )
    private void radarglow_getTeamColor(CallbackInfoReturnable<Integer> cir) {
        Entity self = (Entity) (Object) this;
        if (RadarGlowState.getInstance().isHighlighted(self)) {
            cir.setReturnValue(RadarGlowState.GLOW_COLOR);
            cir.cancel();
        }
    }

    /**
     * Override isGlowing() so highlighted entities actually render with an outline,
     * even if they don't have the Glowing status effect or a Spectral Arrow on them.
     *
     * We keep the original return value (via CallbackInfoReturnable) if the entity
     * is already glowing — we only force-enable it for our highlighted ones.
     */
    @Inject(
            method = "isGlowing",
            at = @At("HEAD"),
            cancellable = true
    )
    private void radarglow_isGlowing(CallbackInfoReturnable<Boolean> cir) {
        Entity self = (Entity) (Object) this;
        if (RadarGlowState.getInstance().isHighlighted(self)) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }
}
