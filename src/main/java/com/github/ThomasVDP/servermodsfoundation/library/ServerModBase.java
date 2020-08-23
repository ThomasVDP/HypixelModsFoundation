package com.github.ThomasVDP.servermodsfoundation.library;

import com.github.ThomasVDP.servermodsfoundation.library.command.ICommandRegister;
import com.github.ThomasVDP.servermodsfoundation.library.events.IEventSubscribeRegister;

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
