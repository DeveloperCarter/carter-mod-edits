package com.DeveloperCarter.timer;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class TimerCommand {
    private static final int MAX_SECONDS = 12 * 60 * 60;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("settimer")
                        .then(Commands.argument("number", IntegerArgumentType.integer(1))
                                .then(Commands.literal("seconds")
                                        .executes(ctx -> set(ctx.getSource(), getPlayer(ctx.getSource()), IntegerArgumentType.getInteger(ctx, "number"), "seconds", ""))
                                        .then(Commands.argument("label", StringArgumentType.greedyString())
                                                .executes(ctx -> set(ctx.getSource(), getPlayer(ctx.getSource()), IntegerArgumentType.getInteger(ctx, "number"), "seconds", StringArgumentType.getString(ctx, "label")))
                                        )
                                )
                                .then(Commands.literal("minutes")
                                        .executes(ctx -> set(ctx.getSource(), getPlayer(ctx.getSource()), IntegerArgumentType.getInteger(ctx, "number"), "minutes", ""))
                                        .then(Commands.argument("label", StringArgumentType.greedyString())
                                                .executes(ctx -> set(ctx.getSource(), getPlayer(ctx.getSource()), IntegerArgumentType.getInteger(ctx, "number"), "minutes", StringArgumentType.getString(ctx, "label")))
                                        )
                                )
                                .then(Commands.literal("hours")
                                        .executes(ctx -> set(ctx.getSource(), getPlayer(ctx.getSource()), IntegerArgumentType.getInteger(ctx, "number"), "hours", ""))
                                        .then(Commands.argument("label", StringArgumentType.greedyString())
                                                .executes(ctx -> set(ctx.getSource(), getPlayer(ctx.getSource()), IntegerArgumentType.getInteger(ctx, "number"), "hours", StringArgumentType.getString(ctx, "label")))
                                        )
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("timer")
                        .then(Commands.literal("status").executes(ctx -> status(ctx.getSource())))
                        .then(Commands.literal("cancel").executes(ctx -> cancel(ctx.getSource())))
        );
    }



    private static int set(CommandSourceStack source, ServerPlayer player, int number, String unit, String label) {
        if (player == null) { source.sendFailure(Component.literal("This command must be run by a player.")); return 0; }
        int seconds = switch (unit) {
            case "seconds" -> number;
            case "minutes" -> number * 60;
            case "hours" -> number * 3600;
            default -> 0;
        };
        if (seconds <= 0) { source.sendFailure(Component.literal("Invalid unit.")); return 0; }
        if (seconds > MAX_SECONDS) { source.sendFailure(Component.literal("Max timer is 12 hours.")); return 0; }
        TimerScheduler.INSTANCE.schedule(player, seconds * 20L, label);
        String msg = "Timer set for " + number + " " + unit + (label.isEmpty() ? "" : " (" + label + ")") + ".";
        source.sendSuccess(() -> Component.literal(msg), false);
        return 1;
    }

    private static int status(CommandSourceStack source) {
        ServerPlayer player = getPlayer(source);
        if (player == null) { source.sendFailure(Component.literal("Must be a player.")); return 0; }

        ServerLevel store = player.getServer().overworld();  // <-- use Overworld
        long ticks = TimerData.get(store).map().getLong(player.getUUID());
        if (ticks <= 0) { source.sendSuccess(() -> Component.literal("No active timer."), false); return 1; }

        long seconds = ticks / 20L;
        long h = seconds / 3600, m = (seconds % 3600) / 60, s = seconds % 60;
        String pretty = (h > 0 ? h + "h " : "") + (m > 0 ? m + "m " : "") + s + "s";

        String lbl = TimerData.get(store).getLabel(player.getUUID());
        String msg = "Time remaining: " + pretty + (lbl.isEmpty() ? "" : " (" + lbl + ")");
        source.sendSuccess(() -> Component.literal(msg), false);
        return 1;
    }

    private static int cancel(CommandSourceStack source) {
        ServerPlayer player = getPlayer(source);
        if (player == null) { source.sendFailure(Component.literal("Must be a player.")); return 0; }
        boolean removed = TimerScheduler.INSTANCE.cancel(player);
        if (removed) source.sendSuccess(() -> Component.literal("Timer cancelled."), false);
        else source.sendSuccess(() -> Component.literal("No active timer."), false);
        return removed ? 1 : 0;
    }

    private static ServerPlayer getPlayer(CommandSourceStack source) {
        try { return source.getPlayerOrException(); } catch (Exception e) { return null; }
    }
}
