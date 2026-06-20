package com.DeveloperCarter.timer.patch;

import com.DeveloperCarter.timer.TimerMod;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/**
 * Registers the {@code /fovpatch} command, which toggles the Artifacts Running
 * Shoes FOV fix on and off (default on).
 *
 * The actual FOV correction lives in
 * {@code com.DeveloperCarter.timer.mixin.RunningShoesFovMixin}, which edits
 * {@code AbstractClientPlayer.getFieldOfViewModifier} at the source so vanilla's
 * own FOV smoothing handles sprint transitions. This class only owns the
 * command + shared {@link FovToggleState} flag.
 */
@EventBusSubscriber(modid = TimerMod.MODID, value = Dist.CLIENT)
public final class FovPatchClient {

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("fovpatch").executes(ctx -> {
                    FovToggleState.enabled = !FovToggleState.enabled;
                    final boolean state = FovToggleState.enabled;
                    ctx.getSource().sendSuccess(
                            () -> Component.literal(
                                    "Running Shoes FOV patch " + (state ? "enabled" : "disabled")),
                            false);
                    return 1;
                }));
    }
}
