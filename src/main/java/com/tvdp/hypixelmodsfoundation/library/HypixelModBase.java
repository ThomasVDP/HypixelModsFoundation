package com.tvdp.hypixelmodsfoundation.library;

import com.tvdp.hypixelmodsfoundation.library.command.ICommandRegister;
import com.tvdp.hypixelmodsfoundation.library.events.IEventSubscribeRegister;

public interface HypixelModBase
{
    /**
     * Called when the event subscribers are initialized
     */
    void onRegisterEventSubscribers(IEventSubscribeRegister subscribeRegister);

    /**
     * Called when the commands of this mod are initialized
     */
    void onRegisterCommands(ICommandRegister commandRegister);
}
