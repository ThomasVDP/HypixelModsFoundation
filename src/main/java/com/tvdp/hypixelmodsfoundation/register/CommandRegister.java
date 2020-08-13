package com.tvdp.hypixelmodsfoundation.register;

import com.google.common.collect.Lists;
import com.tvdp.hypixelmodsfoundation.ServerModsFoundation;
import com.tvdp.hypixelmodsfoundation.command.CommandWrapper;
import com.tvdp.hypixelmodsfoundation.library.command.ICommandRegister;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraftforge.client.ClientCommandHandler;

import java.lang.reflect.Field;
import java.util.*;

public class CommandRegister implements ICommandRegister
{
    private final Map<String, List<String>> addonToCommandName = new HashMap<>();
    private final Map<String, ICommand> commandSet = new HashMap<>();

    private String currentAddonId = "";

    private Field commandSetField;

    public CommandRegister()
    {
        try {
            commandSetField = Lists.newArrayList(CommandHandler.class.getDeclaredFields()).stream().filter(field -> field.getType().equals(Set.class)).findFirst().orElseThrow(() -> new Exception("CommandSet couldn't be found!"));
            commandSetField.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerCommand(ICommand command)
    {
        if (commandSet.containsKey(command.getCommandName())) return;

        //Use a wrapper to automatically make sure the user can run the command
        CommandWrapper wrapper = new CommandWrapper(command, currentAddonId);
        commandSet.put(command.getCommandName(), wrapper);

        //link the commandName with the addonId
        if (!addonToCommandName.containsKey(currentAddonId)) {
            List<String> commands = new ArrayList<>();
            commands.add(command.getCommandName());
            addonToCommandName.put(currentAddonId, commands);
        } else {
            addonToCommandName.get(currentAddonId).add(command.getCommandName());
        }
    }

    public void reload()
    {
        //do nothing when no commands are registered
        if (commandSet.isEmpty()) return;

        //remove all custom commands
        try {
            ((Set<ICommand>)commandSetField.get(ClientCommandHandler.instance)).removeAll(commandSet.values());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        commandSet.forEach((name, command) -> {
            ClientCommandHandler.instance.getCommands().remove(command.getCommandName());
            command.getCommandAliases().forEach(alias -> ClientCommandHandler.instance.getCommands().remove(alias));
        });

        //add active commands
        addonToCommandName.forEach((addonId, commandNames) -> {
            if (ServerModsFoundation.instance.activeModContainers.containsKey(addonId)) {
                commandNames.forEach(name -> {
                    ClientCommandHandler.instance.registerCommand(commandSet.get(name));
                });
            }
        });
    }

    public void setCurrentAddon(String addonName)
    {
        this.currentAddonId = addonName;
    }
}
