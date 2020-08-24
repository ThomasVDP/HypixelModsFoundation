package com.github.ThomasVDP.servermodsfoundation.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.List;

public class CommandWrapper implements ICommand
{
    ICommand command;

    public CommandWrapper(ICommand command, String addonId)
    {
        this.command = command;
    }

    @Override
    public String getCommandName() {
        return command.getCommandName();
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return command.getCommandUsage(sender);
    }

    @Override
    public List<String> getCommandAliases() {
        return command.getCommandAliases();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        command.processCommand(sender, args);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        return command.addTabCompletionOptions(sender, args, pos);
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return command.isUsernameIndex(args, index);
    }

    @Override
    public int compareTo(ICommand o) {
        return command.compareTo(o);
    }
}
