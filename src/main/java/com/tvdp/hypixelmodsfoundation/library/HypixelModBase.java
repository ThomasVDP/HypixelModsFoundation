package com.tvdp.hypixelmodsfoundation.library;

public interface HypixelModBase
{
    /**
     * Called when the event subscribers are initialized
     */
    void onRegisterEventSubscribers();

    /**
     * Called when the commands of this mod are initialized
     */
    void onRegisterCommands();

    /**
     * Called when the mod gets disabled
     */
    void onDisable();
}
