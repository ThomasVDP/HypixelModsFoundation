package com.tvdp.servermodsfoundation.library;

import com.tvdp.servermodsfoundation.library.command.ICommandRegister;
import com.tvdp.servermodsfoundation.library.events.IEventSubscribeRegister;

public interface ServerModBase
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
