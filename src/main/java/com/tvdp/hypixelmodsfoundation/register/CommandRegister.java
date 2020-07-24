package com.tvdp.hypixelmodsfoundation.register;

import com.google.common.collect.Lists;
import com.tvdp.hypixelmodsfoundation.HypixelModsFoundation;
import com.tvdp.hypixelmodsfoundation.command.CommandWrapper;
import com.tvdp.hypixelmodsfoundation.library.command.ICommandRegister;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraftforge.client.ClientCommandHandler;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandRegister implements ICommandRegister
{
    //private String currentAddon = "";
    private Map<String, ICommand> commandSet = new HashMap<>();

    private Field commandSetField;

    public void CommandRegister()
    {
        try {
            commandSetField = Lists.newArrayList(CommandHandler.class.getDeclaredFields()).stream().filter(field -> field.getType().equals(Set.class)).findFirst().orElseThrow(() -> new Exception("CommandSet couldn't be found!"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void registerCommand(ICommand command)
    {
        if (commandSet.containsKey(command.getCommandName())) return;

        CommandWrapper wrapper = new CommandWrapper(command);
        commandSet.put(command.getCommandName(), wrapper);
    }

    public void reload()
    {
        if (!commandSet.isEmpty()) {
            commandSet.values().forEach(command -> {
                try {
                    try {
                        if (commandSetField.get(ClientCommandHandler.instance) != null) {
                            ((Set<ICommand>)commandSetField.get(ClientCommandHandler.instance)).remove(command);
                        }
                    } catch (NullPointerException e) {
                        ;
                    }
                /*if (commandSetField.get(ClientCommandHandler.instance) != null) {
                    ((Set<ICommand>)commandSetField.get(ClientCommandHandler.instance)).remove(command);
                }*/
                    ClientCommandHandler.instance.getCommands().remove(command.getCommandName());
                    command.getCommandAliases().forEach(alias -> ClientCommandHandler.instance.getCommands().remove(alias));
                } catch (IllegalAccessException e) {
                   e.printStackTrace();
                }
            });
        }

        if (HypixelModsFoundation.instance.isOnHypixel)
        {
            commandSet.values().forEach(ClientCommandHandler.instance::registerCommand);
        }
    }

    /*public void setAddon(String addonName)
    {
        this.currentAddon = addonName;
    }*/
}
