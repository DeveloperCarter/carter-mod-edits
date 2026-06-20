package com.DeveloperCarter.timer.patch;

import com.DeveloperCarter.timer.TimerMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Abilities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

/**
 * A client-side "/noclip" toggle: lets the local player fly through blocks while
 * staying in their current game mode.
 *
 * Noclip for your own player has to be client-side, because the client computes
 * its own movement collision. We flip the vanilla {@code noPhysics} flag (which
 * also disables suffocation, since {@code Entity.isInWall()} returns false while
 * it is set) and enable creative-style flight so you do not just fall.
 *
 * Flight is announced to the server via {@code onUpdateAbilities()} so it does
 * not get rejected as illegal flying. This works in singleplayer / LAN; a
 * dedicated server with movement anti-cheat may rubber-band or kick.
 */
@EventBusSubscriber(modid = TimerMod.MODID, value = Dist.CLIENT)
public final class NoclipClient {

    private static boolean enabled = false;
    // True only when this feature turned flight on, so we don't strip flight
    // from a player who can already fly (creative / spectator).
    private static boolean grantedFlight = false;

    @SubscribeEvent
    public static void onRegisterClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("noclip").executes(ctx -> {
                    enabled = !enabled;
                    if (!enabled) disable(Minecraft.getInstance().player);
                    final boolean state = enabled;
                    ctx.getSource().sendSuccess(
                            () -> Component.literal("Noclip " + (state ? "enabled" : "disabled")), false);
                    return 1;
                }));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        if (!enabled) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        player.noPhysics = true;

        Abilities abilities = player.getAbilities();
        if (!abilities.mayfly) {
            abilities.mayfly = true;
            grantedFlight = true;
            player.onUpdateAbilities();
        }
        abilities.flying = true;
        player.fallDistance = 0.0F;
    }

    private static void disable(LocalPlayer player) {
        if (player == null) return;
        player.noPhysics = false;
        if (grantedFlight) {
            Abilities abilities = player.getAbilities();
            abilities.flying = false;
            abilities.mayfly = false;
            player.onUpdateAbilities();
            grantedFlight = false;
        }
    }
}
