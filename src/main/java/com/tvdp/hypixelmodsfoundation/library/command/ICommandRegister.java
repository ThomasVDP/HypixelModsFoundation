package com.tvdp.hypixelmodsfoundation.library.command;

import net.minecraft.command.ICommand;

public interface ICommandRegister
{
    void registerCommand(String addonName, ICommand command);
}
