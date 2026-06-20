package com.DeveloperCarter.timer.patch;

import com.DeveloperCarter.timer.TimerMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Removes the camera "zoom" caused by the Artifacts Running Shoes.
 *
 * The shoes have no FOV setting of their own. The zoom is a vanilla side effect
 * of the movement-speed boost they apply while sprinting: GameRenderer.getFov
 * multiplies the base FOV by the player's speed-based modifier before the
 * ComputeFov event fires. The boost is a transient MOVEMENT_SPEED modifier with
 * the id "artifacts:sprinting_speed".
 *
 * Whenever that modifier is active we override the computed FOV back to the
 * player's configured base FOV, which cancels the speed-driven widening while
 * leaving the actual speed boost untouched.
 *
 * Looking at the attribute (rather than the worn item) means this works no
 * matter which slot mod the shoes are equipped through. On ATM10 they sit in an
 * Accessories slot, not a Curios slot, which is why the old item-lookup never
 * detected them.
 */
@EventBusSubscriber(modid = TimerMod.MODID, value = Dist.CLIENT)
public final class FovPatchClient {

    // Artifacts.id("sprinting_speed") -> the Running Shoes' sprint speed modifier.
    private static final ResourceLocation RUNNING_SHOES_SPRINT_SPEED =
            ResourceLocation.fromNamespaceAndPath("artifacts", "sprinting_speed");

    @SubscribeEvent
    public static void onFov(ViewportEvent.ComputeFov event) {
        if (!(event.getCamera().getEntity() instanceof LocalPlayer player)) return;

        AttributeInstance speed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) return;

        if (speed.getModifier(RUNNING_SHOES_SPRINT_SPEED) != null) {
            event.setFOV(Minecraft.getInstance().options.fov().get());
        }
    }
}
