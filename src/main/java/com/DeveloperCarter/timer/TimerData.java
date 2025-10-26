package com.DeveloperCarter.timer;

import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.UUID;

public class TimerData extends SavedData {
    private static final String DATA_NAME = "minecraft_timer_data";
    private final Object2LongOpenHashMap<UUID> remaining = new Object2LongOpenHashMap<>();
    private final Object2ObjectOpenHashMap<UUID, String> labels = new Object2ObjectOpenHashMap<>();

    public TimerData() {}

    public static TimerData load(CompoundTag tag, HolderLookup.Provider provider) {
        TimerData data = new TimerData();
        ListTag list = tag.getList("timers", 10);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag ct = list.getCompound(i);
            UUID id = ct.getUUID("uuid");
            long ticks = ct.getLong("ticks");
            data.remaining.put(id, ticks);
            if (ct.contains("label")) data.labels.put(id, ct.getString("label"));
        }
        return data;
    }

    public static TimerData get(ServerLevel level) {
        SavedData.Factory<TimerData> factory = new SavedData.Factory<>(TimerData::new, TimerData::load);
        return level.getDataStorage().computeIfAbsent(factory, DATA_NAME);
    }

    public Object2LongOpenHashMap<UUID> map() { return remaining; }
    public String getLabel(UUID id) { return labels.getOrDefault(id, ""); }

    public void set(UUID id, long ticks, String label) {
        if (ticks <= 0) {
            remaining.removeLong(id);
            labels.remove(id);
        } else {
            remaining.put(id, ticks);
            if (label != null) labels.put(id, label);
        }
        setDirty();
    }

    public void remove(UUID id) {
        remaining.removeLong(id);
        labels.remove(id);
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        remaining.object2LongEntrySet().forEach(e -> {
            CompoundTag ct = new CompoundTag();
            ct.putUUID("uuid", e.getKey());
            ct.putLong("ticks", e.getLongValue());
            String lbl = labels.getOrDefault(e.getKey(), "");
            if (!lbl.isEmpty()) ct.putString("label", lbl);
            list.add(ct);
        });
        tag.put("timers", list);
        return tag;
    }
}
