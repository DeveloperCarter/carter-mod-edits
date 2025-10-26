package com.DeveloperCarter.timer;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.minecraft.world.level.Level;

import java.util.UUID;

public enum TimerScheduler {
    INSTANCE;

    private final Object2LongOpenHashMap<UUID> alertTicks = new Object2LongOpenHashMap<>();

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post e) {
        var server = e.getServer();
        ServerLevel level = server.overworld();
        if (level == null) return;

        TimerData data = TimerData.get(level);
        var toRemove = new java.util.ArrayList<UUID>();
        for (Object2LongMap.Entry<UUID> entry : data.map().object2LongEntrySet()) {
            UUID uuid = entry.getKey();
            long remaining = entry.getLongValue() - 1;

            if (remaining <= 0) {
                ServerPlayer p = server.getPlayerList().getPlayer(uuid);
                if (p != null) {
                    String lbl = data.getLabel(uuid);
                    String msg = lbl.isEmpty() ? "\u23F0 Timer done!" : "\u23F0 Timer \"" + lbl + "\" done!";
                    p.displayClientMessage(Component.literal(msg), true);
                    alertTicks.put(uuid, 100L);
                }
                toRemove.add(uuid);
            } else {
                entry.setValue(remaining);
                data.setDirty();
            }
        }
        for (UUID id : toRemove) data.remove(id);

        alertTicks.object2LongEntrySet().removeIf(entry -> {
            UUID uuid = entry.getKey();
            long remain = entry.getLongValue();
            ServerPlayer p = server.getPlayerList().getPlayer(uuid);
            if (p == null) return true;

            if (remain % 20L == 0L)
                p.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 1.0f, 1.0f);

            remain--;
            if (remain <= 0) return true;
            entry.setValue(remain);
            return false;
        });
    }

    public void schedule(ServerPlayer player, long delayTicks, String label) {
        ServerLevel store = player.getServer().overworld();  // <-- use Overworld
        TimerData.get(store).set(player.getUUID(), Math.max(1, delayTicks), label == null ? "" : label);
        alertTicks.remove(player.getUUID());
        player.displayClientMessage(Component.literal("\u23F3 Timer started."), true);
    }

    public boolean cancel(ServerPlayer player) {
        ServerLevel store = player.getServer().overworld();  // <-- use Overworld
        TimerData data = TimerData.get(store);
        boolean had = data.map().removeLong(player.getUUID()) != 0;
        if (had) data.remove(player.getUUID());
        alertTicks.remove(player.getUUID());
        return had;
    }
}
