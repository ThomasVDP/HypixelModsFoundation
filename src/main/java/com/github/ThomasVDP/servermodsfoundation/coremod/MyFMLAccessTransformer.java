package com.github.ThomasVDP.servermodsfoundation.coremod;

import com.google.common.collect.Lists;
import com.google.common.io.Resources;
import net.minecraftforge.fml.common.asm.transformers.AccessTransformer;
import net.minecraftforge.fml.relauncher.CoreModManager;
import org.apache.logging.log4j.core.helpers.Charsets;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.stream.Collectors;

public class MyFMLAccessTransformer extends AccessTransformer
{
    public MyFMLAccessTransformer() throws IOException
    {
        this.readConfigs();
    }

    private void readConfigs()
    {
        try {
            Field mcDirField = CoreModManager.class.getDeclaredField("mcDir");
            mcDirField.setAccessible(true);
            File mcDir = (File)mcDirField.get(null);

            File addonDir = new File(mcDir.getAbsolutePath() + "/CustomServerMods");
            if (addonDir.exists() && addonDir.isDirectory()) {
                List<File> files = Lists.newArrayList(addonDir.listFiles()).stream().filter(file -> file.getName().matches(".*\\.jar")).collect(Collectors.toList());

                files.forEach(file -> {
                    try {
                        URLClassLoader child = new URLClassLoader(new URL[] {file.toURI().toURL()}, null);
                        if (child.getResource("forge_at.cfg") != null) {
                            System.out.println("Processed " + file.getName() + "'s forge_at.cfg file!");
                            this.processATFile(Resources.asCharSource(child.getResource("forge_at.cfg"), Charsets.UTF_8));
                        }
                        /*try {
                            Class.forName("net.labymod.main.LabyMod");

                            if (child.getResource("labymod_accesstransformer.cfg") != null) {
                                System.out.println("Processed Labymod_at.cfg in " + file.getName() + "!");
                                this.processATFile(Resources.asCharSource(child.getResource("labymod_accesstransformer.cfg"), Charsets.UTF_8));
                            }
                        } catch (ClassNotFoundException e) {
                            //no labymod
                        }*/
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
