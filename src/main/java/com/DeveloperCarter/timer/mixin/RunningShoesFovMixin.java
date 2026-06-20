package com.DeveloperCarter.timer.mixin;

import com.DeveloperCarter.timer.patch.FovToggleState;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Removes the camera zoom that the Artifacts Running Shoes cause, at its source.
 *
 * {@code AbstractClientPlayer.getFieldOfViewModifier()} widens FOV based on the
 * player's movement speed. The shoes add a transient MOVEMENT_SPEED modifier
 * ("artifacts:sprinting_speed") while sprinting. We scale the returned modifier
 * by the ratio of the speed term computed without that modifier vs. with it,
 * which cancels exactly the shoes' contribution while leaving the normal sprint
 * widening (and flying / bow-scope effects) intact.
 *
 * Correcting the value here, rather than overriding the final FOV after the
 * fact, lets vanilla's existing FOV smoothing ramp it in and out, so there is no
 * pop when you start or stop sprinting.
 */
@Mixin(AbstractClientPlayer.class)
public abstract class RunningShoesFovMixin {

    private static final ResourceLocation CARTER$SHOES_SPRINT_SPEED =
            ResourceLocation.fromNamespaceAndPath("artifacts", "sprinting_speed");

    @Inject(method = "getFieldOfViewModifier", at = @At("RETURN"), cancellable = true)
    private void carter$removeRunningShoesFov(CallbackInfoReturnable<Float> cir) {
        if (!FovToggleState.enabled) return;

        AbstractClientPlayer self = (AbstractClientPlayer) (Object) this;
        AttributeInstance speed = self.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null || speed.getModifier(CARTER$SHOES_SPRINT_SPEED) == null) return;

        float walk = self.getAbilities().getWalkingSpeed();
        if (walk == 0.0F) return;

        double full = speed.getValue();
        double without = carter$speedWithout(speed, CARTER$SHOES_SPRINT_SPEED);

        // Mirrors the speed term in getFieldOfViewModifier: (speed / walk + 1) / 2.
        float fullTerm = ((float) (full / walk) + 1.0F) / 2.0F;
        float withoutTerm = ((float) (without / walk) + 1.0F) / 2.0F;
        if (fullTerm <= 0.0F || Float.isNaN(fullTerm) || Float.isNaN(withoutTerm)) return;

        cir.setReturnValue(cir.getReturnValueF() * (withoutTerm / fullTerm));
    }

    /** Re-runs the vanilla attribute calculation, skipping the given modifier. */
    private static double carter$speedWithout(AttributeInstance inst, ResourceLocation skip) {
        double d0 = inst.getBaseValue();
        for (AttributeModifier m : inst.getModifiers()) {
            if (m.operation() == AttributeModifier.Operation.ADD_VALUE) {
                d0 += m.amount();
            }
        }
        double d1 = d0;
        for (AttributeModifier m : inst.getModifiers()) {
            if (m.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_BASE && !m.id().equals(skip)) {
                d1 += d0 * m.amount();
            }
        }
        for (AttributeModifier m : inst.getModifiers()) {
            if (m.operation() == AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                d1 *= 1.0 + m.amount();
            }
        }
        return d1;
    }
}
