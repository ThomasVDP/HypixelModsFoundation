package com.tvdp.servermodsfoundation.coremod;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@IFMLLoadingPlugin.SortingIndex(9999)
@IFMLLoadingPlugin.TransformerExclusions("temp.tvdp.hypixelmodsfoundation.coremod.")
public class MyFMLPlugin implements IFMLLoadingPlugin
{
    @Override
    public String[] getASMTransformerClass() {
        System.out.println("*********** Getting classes ready for ASM!");
        List<String> transformers = new ArrayList<>();

        File minecraftDir;
        Field minecraftDirField = ReflectionHelper.findField(Loader.class, "minecraftDir");
        try {
            minecraftDir = (File) minecraftDirField.get(Loader.class);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return new String[0];
        }
        File addonDir = new File(minecraftDir + "/CustomServerMods");
        if (addonDir.exists() && addonDir.isDirectory()) {
            List<File> files = Lists.newArrayList(addonDir.listFiles()).stream().filter(file -> file.getName().matches(".*\\.jar")).collect(Collectors.toList());

            //temporary storage for mod classloader
            Map<File, String> temporary = new HashMap<>();

            //
            // read the info.json from each jar file and look for a transformerClass
            //
            files.forEach(file -> {
                try {
                    URLClassLoader child = new URLClassLoader(new URL[]{file.toURI().toURL()}, this.getClass().getClassLoader());
                    if (child.getResource("info.json") != null) {
                        String fileContents = new BufferedReader(new InputStreamReader(child.getResourceAsStream("info.json"), StandardCharsets.UTF_8)).lines().collect(Collectors.joining("\n"));
                        JsonObject obj = new Gson().fromJson(fileContents, JsonObject.class);
                        if (obj.has("Transformer-Class")) {
                            temporary.put(file, obj.get("Transformer-Class").getAsString());
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            temporary.forEach((file, clazz) -> {
                try {
                    Loader.instance().getModClassLoader().addFile(file);
                    transformers.add(clazz);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            });
        }

        return transformers.toArray(new String[0]);
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
