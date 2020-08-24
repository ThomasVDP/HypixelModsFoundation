package com.github.ThomasVDP.servermodsfoundation.container;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.VersionRange;

import java.io.File;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.*;
import java.util.stream.Collectors;

public class AddonModContainer implements ModContainer
{
    private ModMetadata md;
    private String guiFactory;

    public AddonModContainer(JsonObject infoJson)
    {
        md = new ModMetadata();
        md.modId = "servermod:" + infoJson.get("id").getAsString();
        md.name = infoJson.get("name").getAsString();
        md.description = infoJson.get("description").getAsString();
        md.version = infoJson.get("version").getAsString();
        try {
            md.authorList = Lists.newArrayList(infoJson.get("author").getAsJsonArray()).stream().map(JsonElement::getAsString).collect(Collectors.toList());
        } catch (NullPointerException e) {
            md.authorList = Collections.emptyList();
        }
        md.credits = infoJson.get("credits").getAsString();

        this.guiFactory = infoJson.has("guiFactory") ? infoJson.get("guiFactory").getAsString() : null;
    }

    @Override
    public String getModId() {
        return md.modId;
    }

    @Override
    public String getName() {
        return md.name;
    }

    @Override
    public String getVersion() {
        return md.version;
    }

    @Override
    public String getGuiClassName() {
        return guiFactory;
    }

    @Override
    public String getDisplayVersion() {
        return md.version;
    }

    @Override
    public File getSource() {
        return null;
    }


    @Override
    public Map<String, String> getSharedModDescriptor() {
        return null;
    }

    @Override
    public ModMetadata getMetadata() {
        return md;
    }

    @Override
    public void bindMetadata(MetadataCollection mc) {
    }

    @Override
    public void setEnabledState(boolean enabled) {
    }

    @Override
    public Set<ArtifactVersion> getRequirements() {
        return Collections.emptySet();
    }

    @Override
    public List<ArtifactVersion> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public List<ArtifactVersion> getDependants() {
        return Collections.emptyList();
    }

    @Override
    public String getSortingRules() {
        return "";
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return false;
    }

    @Override
    public boolean matches(Object mod) {
        return false;
    }

    @Override
    public Object getMod() {
        return new Object();
    }

    @Override
    public ArtifactVersion getProcessedVersion() {
        return null;
    }

    @Override
    public boolean isImmutable() {
        return false;
    }

    @Override
    public VersionRange acceptableMinecraftVersionRange() {
        return Loader.instance().getMinecraftModContainer().getStaticVersionRange();
    }

    @Override
    public Certificate getSigningCertificate() {
        return null;
    }

    @Override
    public Map<String, String> getCustomModProperties() {
        return EMPTY_PROPERTIES;
    }

    @Override
    public Class<?> getCustomResourcePackClass() {
        return null;
    }

    @Override
    public Disableable canBeDisabled() {
        return Disableable.NEVER;
    }

    @Override
    public List<String> getOwnedPackages() {
        return ImmutableList.of();
    }

    @Override
    public boolean shouldLoadInEnvironment() {
        return false;
    }

    @Override
    public URL getUpdateUrl() {
        return null;
    }
}
