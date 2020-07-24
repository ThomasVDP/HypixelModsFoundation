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
import net.minecraftforge.fml.common.event.FMLEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION)
public class HypixelModsFoundation
{
    public boolean isOnHypixel = false;
    private final Map<String, HypixelModBase> hpAddons = new HashMap<>();
    private final List<AddonModContainer> addonContainers = new ArrayList<>();
    private final CommandRegister commandRegister = new CommandRegister();
    private final EventSubscriberRegister subscriberRegister = new EventSubscriberRegister();

    private Field activeModList;
    private Field eventChannels;
    private Field masterChannel;
    private Field modController;

    @Mod.Instance
    public static HypixelModsFoundation instance;

    public HypixelModsFoundation()
    {
        this.loadAddons();

        try {
            this.activeModList = LoadController.class.getDeclaredField("activeModList");
            this.activeModList.setAccessible(true);
            this.modController = Loader.class.getDeclaredField("modController");
            this.modController.setAccessible(true);
            this.masterChannel = LoadController.class.getDeclaredField("masterChannel");
            this.masterChannel.setAccessible(true);
            this.eventChannels = LoadController.class.getDeclaredField("eventChannels");
            this.eventChannels.setAccessible(true);
        } catch (NoSuchFieldException e) {
            //e.printStackTrace();
            System.out.println("Failed to get activeModList field");
        }
    }

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event)
    {
        System.out.println("******** Pre init");

        hpAddons.forEach((name, addon) -> {
            //this.commandRegister.setAddon(name);
            addon.onRegisterCommands(this.commandRegister);
        });
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        MinecraftForge.EVENT_BUS.register(this);
        hpAddons.forEach((name, addon) -> {
            this.subscriberRegister.setAddon(name);
            addon.onRegisterEventSubscribers(this.subscriberRegister);
        });
    }

    @Mod.EventHandler
    public void finishLoading(FMLLoadCompleteEvent event)
    {
        try {
            Map<String, EventBus> temporary = Maps.newHashMap((ImmutableMap<String, EventBus>)eventChannels.get(this.modController.get(Loader.instance())));
            this.addonContainers.forEach(addonModContainer -> temporary.put(addonModContainer.getModId(), new EventBus(addonModContainer.getModId())));
            this.eventChannels.set(this.modController.get(Loader.instance()), ImmutableMap.copyOf(temporary));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.out.println("ERROR: Couldn't get eventChannels!");
        }
    }

    @SubscribeEvent
    public void onConnectToServer(FMLNetworkEvent.ClientConnectedToServerEvent event)
    {
        if (event.manager.channel().remoteAddress() instanceof InetSocketAddress) {
            this.isOnHypixel = ((InetSocketAddress)event.manager.channel().remoteAddress()).getHostName().contains("hypixel.net");
            System.out.println(this.isOnHypixel ? "Connected to Hypixel!" : "Not connected to Hypixel!");
        } else {
            this.isOnHypixel = false;
            System.out.println("Couldn't get server adress!");
        }

        try {
            if (this.isOnHypixel) {
                ((List<ModContainer>)this.activeModList.get(this.modController.get(Loader.instance()))).addAll(addonContainers);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.out.println("ERROR: couldn't load mods in modContainer");
        }

        commandRegister.reload();
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event)
    {
        System.out.println("TEst");
        try {
            ((List<ModContainer>)this.activeModList.get(this.modController.get(Loader.instance()))).removeAll(addonContainers);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            System.out.println("ERROR: Couldn't remove mod containers");
        }
    }

    private void loadAddons()
    {
        File addonDir = new File(Loader.instance().getConfigDir().getParentFile().getAbsolutePath() + "/HypixelMods");
        if (addonDir.exists() && addonDir.isDirectory()) {
            List<File> files = Lists.newArrayList(addonDir.listFiles()).stream().filter(file -> file.getName().matches(".*\\.jar")).collect(Collectors.toList());

            Map<String, String> temporary = new HashMap<>();

            files.forEach(file -> {
                try {
                    System.out.println("Found addon: " + file.getName());
                    URLClassLoader child = new URLClassLoader(new URL[] {file.toURI().toURL()}, this.getClass().getClassLoader());
                    for (URL url : child.getURLs()) {
                        System.out.println(url.getPath());
                    }
                    if (child.getResource("info.json") != null) {
                        String fileContents = new BufferedReader(new InputStreamReader(child.getResourceAsStream("info.json"), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
                        JsonObject obj = new Gson().fromJson(fileContents, JsonObject.class);
                        if (obj.get("id") == null) {
                            System.out.println("ERROR: no identifier found for addon: " + file.getName());
                            return;
                        }
                        String addonName = obj.get("id").getAsString();
                        addonContainers.add(new AddonModContainer(obj));
                        System.out.println("Loaded addon: " + addonName);
                        temporary.put(addonName, obj.get("Main-Class").getAsString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });


            files.forEach(file -> {
                try {
                    Loader.instance().getModClassLoader().addFile(file);
                } catch (MalformedURLException e) {
                    //e.printStackTrace();
                    System.out.println("Error: Couldn't load addon file");
                }
            });

            temporary.forEach((addonName, className) -> {
                try {
                    Class classToLoad = Class.forName(className, true, Loader.instance().getModClassLoader());
                    if (HypixelModBase.class.isAssignableFrom(classToLoad)) {
                        this.hpAddons.put(addonName, (HypixelModBase)classToLoad.newInstance());
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
