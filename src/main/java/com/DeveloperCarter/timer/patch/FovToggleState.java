package com.DeveloperCarter.timer.patch;

/**
 * Shared on/off flag for the Running Shoes FOV patch.
 *
 * Toggled by the {@code /fovpatch} command (see {@link FovPatchClient}) and read
 * by the FOV mixin. Kept in its own tiny class with no Minecraft/NeoForge
 * imports so the mixin can reference it safely during early class loading.
 */
public final class FovToggleState {
    public static volatile boolean enabled = true;

    private FovToggleState() {}
}
