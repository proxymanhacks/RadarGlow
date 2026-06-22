package net.radarglow.mixin;

import net.minecraft.entity.Entity;
import net.radarglow.RadarGlowState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {

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
