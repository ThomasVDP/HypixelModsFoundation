package com.tvdp.hypixelmodsfoundation;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tvdp.hypixelmodsfoundation.container.AddonModContainer;
import com.tvdp.hypixelmodsfoundation.library.HypixelModBase;
import com.tvdp.hypixelmodsfoundation.register.CommandRegister;
import com.tvdp.hypixelmodsfoundation.register.EventSubscriberRegister;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION)
public class HypixelModsFoundation
{
    private final Map<String, String> addonExcludeServerLinks = new HashMap<>();
    private final Map<String, String> addonIncludeServerLinks = new HashMap<>();
    /**
     * list of the instantiated mod objects
     */
    private final Map<String, HypixelModBase> addonObjects = new HashMap<>();
    /**
     * list of ALL the modContainers
     */
    private final Map<String, AddonModContainer> addonContainers = new HashMap<>();
    /**
     * list of the active mods on the server
     */
    public final Map<String, ModContainer> activeModContainers = new HashMap<>();

    /**
     * specific registers
     */
    private final CommandRegister commandRegister = new CommandRegister();
    private final EventSubscriberRegister subscriberRegister = new EventSubscriberRegister();

    /**
     * Fields for reflection
     */
    private Field activeModListField;
    private Field eventChannelsField;
    private Field masterChannelField;
    private Field modControllerField;

    @Mod.Instance
    public static HypixelModsFoundation instance;

    public HypixelModsFoundation()
    {
        this.loadAddons();

        try {
            this.activeModListField = LoadController.class.getDeclaredField("activeModList");
            this.activeModListField.setAccessible(true);
            this.modControllerField = Loader.class.getDeclaredField("modController");
            this.modControllerField.setAccessible(true);
            this.masterChannelField = LoadController.class.getDeclaredField("masterChannel");
            this.masterChannelField.setAccessible(true);
            this.eventChannelsField = LoadController.class.getDeclaredField("eventChannels");
            this.eventChannelsField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            //e.printStackTrace();
            System.out.println("Failed to get activeModList field");
        }
    }

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event)
    {
        System.out.println("******** Pre init");

        addonObjects.forEach((name, addon) -> {
            this.commandRegister.setCurrentAddon(name);
            addon.onRegisterCommands(this.commandRegister);
        });
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        addonObjects.forEach((name, addon) -> {
            this.subscriberRegister.setAddon(name);
            addon.onRegisterEventSubscribers(this.subscriberRegister);
        });
    }

    @Mod.EventHandler
    public void finishLoading(FMLLoadCompleteEvent event)
    {
        try {
            Map<String, EventBus> temporary = Maps.newHashMap((ImmutableMap<String, EventBus>) eventChannelsField.get(this.modControllerField.get(Loader.instance())));
            this.addonContainers.values().forEach(addonModContainer -> temporary.put(addonModContainer.getModId(), new EventBus(addonModContainer.getModId())));
            this.eventChannelsField.set(this.modControllerField.get(Loader.instance()), ImmutableMap.copyOf(temporary));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.out.println("ERROR: Couldn't get eventChannels!");
        }
    }

    @SubscribeEvent
    public void onConnectToServer(FMLNetworkEvent.ClientConnectedToServerEvent event)
    {
        if (!(event.manager.channel().remoteAddress() instanceof InetSocketAddress)) return;
        String hostname = ((InetSocketAddress)event.manager.channel().remoteAddress()).getHostString();
        System.out.println("Connected to " + hostname);

        //clear the active modList
        activeModContainers.clear();
        /*
          Get the addon if in an included server
         */
        addonIncludeServerLinks.forEach((host, addonId) -> {
            if (hostname.contains(host)) {
                activeModContainers.put(addonId, addonContainers.get(addonId));
            }
        });
        /*
          Get the addon if not on an excluded server
         */
        addonExcludeServerLinks.forEach((host, addonId) -> {
            if (!hostname.contains(host)) {
                activeModContainers.put(addonId, addonContainers.get(addonId));
            }
        });

        /*
          Add the modContainers to the forge list
         */
        try {
            ((List<ModContainer>)this.activeModListField.get(this.modControllerField.get(Loader.instance()))).addAll(activeModContainers.values());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.out.println("ERROR: couldn't load mods in modContainer");
        }

        commandRegister.reload();
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        try {
            ((List<ModContainer>)this.activeModListField.get(this.modControllerField.get(Loader.instance()))).removeAll(activeModContainers.values());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.out.println("ERROR: Couldn't remove mod containers");
        }

        //clear the active modList
        activeModContainers.clear();
    }

    private void loadAddons()
    {
        File addonDir = new File(Loader.instance().getConfigDir().getParentFile().getAbsolutePath() + "/HypixelMods");
        if (addonDir.exists() && addonDir.isDirectory()) {
            List<File> files = Lists.newArrayList(addonDir.listFiles()).stream().filter(file -> file.getName().matches(".*\\.jar")).collect(Collectors.toList());

            Map<String, String> temporary = new HashMap<>();

            //
            // read the info.json from the jar file and make a modContainer
            //
            files.forEach(file -> {
                try {
                    System.out.println("Found addon: " + file.getName());
                    URLClassLoader child = new URLClassLoader(new URL[] {file.toURI().toURL()}, this.getClass().getClassLoader());
                    if (child.getResource("info.json") != null) {
                        String fileContents = new BufferedReader(new InputStreamReader(child.getResourceAsStream("info.json"), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
                        JsonObject obj = new Gson().fromJson(fileContents, JsonObject.class);
                        if (obj.get("id") == null) {
                            System.out.println("ERROR: no identifier found for addon: " + file.getName());
                            return;
                        }
                        String addonName = obj.get("id").getAsString();
                        addonContainers.put(addonName, new AddonModContainer(obj));
                        System.out.println("Loaded addon: " + addonName);
                        temporary.put(addonName, obj.get("Main-Class").getAsString());
                        if (obj.has("includeServers")) {
                            if (obj.get("includeServers").getAsJsonArray().size() != 0) {
                                obj.get("includeServers").getAsJsonArray().forEach(element -> {
                                    String hostname = new InetSocketAddress(element.getAsString(), 0).getHostString();
                                    addonIncludeServerLinks.put(hostname, addonName);
                                });
                            }
                        } else if (obj.has("excludeServers")) {
                            if (obj.get("excludeServers").getAsJsonArray().size() != 0) {
                                obj.get("excludeServers").getAsJsonArray().forEach(element -> {
                                    String hostname = new InetSocketAddress(element.getAsString(), 0).getHostString();
                                    addonExcludeServerLinks.put(hostname, addonName);
                                });
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            //
            // Add the jar file locations to the mod classloader
            //
            files.forEach(file -> {
                try {
                    Loader.instance().getModClassLoader().addFile(file);
                } catch (MalformedURLException e) {
                    //e.printStackTrace();
                    System.out.println("Error: Couldn't load addon file");
                }
            });

            //
            // load the addons with the correct classLoader
            //
            temporary.forEach((addonName, className) -> {
                try {
                    Class classToLoad = Class.forName(className, true, Loader.instance().getModClassLoader());
                    if (HypixelModBase.class.isAssignableFrom(classToLoad)) {
                        this.addonObjects.put(addonName, (HypixelModBase)classToLoad.newInstance());
                    } else {
                        System.out.println("ERROR: Couldn't load hypixel addon! Invalid class type!");
                    }
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    System.out.println("ERROR: Couldn't load addon class");
                }
            });
        }
    }
}
