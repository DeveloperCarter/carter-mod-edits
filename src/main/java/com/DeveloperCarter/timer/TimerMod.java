package com.DeveloperCarter.timer;

import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;

@Mod(TimerMod.MODID)
public class TimerMod {
    public static final String MODID = "carter_mod";

    public TimerMod() {
        NeoForge.EVENT_BUS.register(this);
        NeoForge.EVENT_BUS.register(TimerScheduler.INSTANCE);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        TimerCommand.register(event.getDispatcher());
    }
}
