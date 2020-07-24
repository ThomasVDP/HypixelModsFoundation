package com.tvdp.hypixelmodsfoundation.coremod;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.SortingIndex(9999)
@IFMLLoadingPlugin.TransformerExclusions("temp.tvdp.hypixelmodsfoundation.coremod.")
public class MyFMLPlugin implements IFMLLoadingPlugin
{
    @Override
    public String[] getASMTransformerClass() {
        System.out.println("*********** Getting classes ready for ASM!");
        return new String[0];
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
