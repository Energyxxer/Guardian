package com.energyxxer.guardian.ui.commodoreresources;

import com.energyxxer.commodore.defpacks.DefinitionPack;
import com.energyxxer.commodore.util.io.DirectoryCompoundInput;
import com.energyxxer.commodore.util.io.ZipCompoundInput;
import com.energyxxer.commodore.versioning.BedrockEditionVersion;
import com.energyxxer.commodore.versioning.JavaEditionVersion;
import com.energyxxer.commodore.versioning.ThreeNumberVersion;
import com.energyxxer.commodore.versioning.Version;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.util.logger.Debug;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefinitionPacks {
    private static HashMap<String, DefinitionPack> loadedDefinitionPacks = new LinkedHashMap<>();
    private static final Pattern javaPackKey = Pattern.compile("minecraft_j_1_(\\d+)(?:_(\\d+))?");
    private static final Pattern bedrockPackKey = Pattern.compile("minecraft_b_1_(\\d+)(?:_(\\d+))?");

    private static JavaEditionVersion latestKnownJavaVersion = new JavaEditionVersion(1, 13, 0);
    private static BedrockEditionVersion latestKnownBedrockVersion = new BedrockEditionVersion(1, 13, 0);
    private static Version[] knownVersionList = null;

    public static void loadAll() {
        loadedDefinitionPacks.clear();

        File defPackDir = Guardian.core.getDefinitionPacksDir();
        defPackDir.mkdirs();
        File[] files = defPackDir.listFiles();
        if(files != null) {
            for(File file : files) {
                DefinitionPack defPack = null;
                if(file.isDirectory()) {
                    defPack = new DefinitionPack(new DirectoryCompoundInput(file));
                    String packName = file.getName();
                    loadedDefinitionPacks.put(packName, defPack);
                    updateLatestKnownVersion(packName);
                } else if(file.isFile() && file.getName().endsWith(".zip")) {
                    defPack = new DefinitionPack(new ZipCompoundInput(file));
                    String packName = file.getName().substring(0, file.getName().length() - ".zip".length());
                    loadedDefinitionPacks.put(packName, defPack);
                    updateLatestKnownVersion(packName);
                }
                try {
                    if(defPack != null) defPack.load();
                } catch(Exception e) {
                    Debug.log(e.getMessage(), Debug.MessageType.ERROR);
                }
            }
        }

        Debug.log("Loaded definition packs: " + loadedDefinitionPacks);
    }

    private static void updateLatestKnownVersion(String packName) {
        Version version = getVersionForPackName(packName);
        if(version == null) return;
        if(version instanceof JavaEditionVersion) {
            if(latestKnownJavaVersion.compare(version) < 0) {
                latestKnownJavaVersion = (JavaEditionVersion) version;
                knownVersionList = null;
            }
        }
        if(version instanceof BedrockEditionVersion) {
            if(latestKnownBedrockVersion.compare(version) < 0) {
                latestKnownBedrockVersion = (BedrockEditionVersion) version;
                knownVersionList = null;
            }
        }
    }

    private static Version getVersionForPackName(String packName) {
        Matcher match = javaPackKey.matcher(packName);
        if(match.matches()) {
            int minor = Integer.parseInt(match.group(1));
            int patch = 0;
            if(match.group(2) != null) {
                patch = Integer.parseInt(match.group(2));
            }
            return new JavaEditionVersion(1, minor, patch);
        }
        match = bedrockPackKey.matcher(packName);
        if(match.matches()) {
            int minor = Integer.parseInt(match.group(1));
            int patch = 0;
            if(match.group(2) != null) {
                patch = Integer.parseInt(match.group(2));
            }
            return new BedrockEditionVersion(1, minor, patch);
        }
        return null;
    }

    public static Version[] getKnownVersions() {
        if(knownVersionList != null) return knownVersionList;
        LinkedHashSet<Version> all = new LinkedHashSet<>();
        for(String key : loadedDefinitionPacks.keySet()) {
            all.add(getVersionForPackName(key));
        }
        knownVersionList = all.toArray(new Version[0]);
        return knownVersionList;
    }

    public static JavaEditionVersion[] getKnownJavaVersions() {
        return Arrays.stream(getKnownVersions()).filter(v -> v instanceof JavaEditionVersion).map(v -> (JavaEditionVersion) v).toArray(JavaEditionVersion[]::new);
    }

    public static BedrockEditionVersion[] getKnownBedrockVersions() {
        return Arrays.stream(getKnownVersions()).filter(v -> v instanceof BedrockEditionVersion).map(v -> (BedrockEditionVersion) v).toArray(BedrockEditionVersion[]::new);
    }

    public static DefinitionPack[] pickPacksForVersion(ThreeNumberVersion targetVersion) {
        if(targetVersion == null) return null;

        String key = "minecraft_" +
                targetVersion.getEditionString().toLowerCase(Locale.ENGLISH).charAt(0) +
                "_" + targetVersion.getMajor() +
                "_" + targetVersion.getMinor() +
                "_" + targetVersion.getPatch();
        DefinitionPack pack = loadedDefinitionPacks.get(key);
        Debug.log("key: " + key);
        if(pack != null) return new DefinitionPack[] {pack};

        key = "minecraft_" +
                targetVersion.getEditionString().toLowerCase(Locale.ENGLISH).charAt(0) +
                "_" + targetVersion.getMajor() +
                "_" + targetVersion.getMinor();
        pack = loadedDefinitionPacks.get(key);
        Debug.log("key: " + key);
        if(pack != null) return new DefinitionPack[] {pack};

        Debug.log("oh no pack is null");

        Map.Entry<Version, DefinitionPack> latestMatch = null;

        for(Map.Entry<String, DefinitionPack> entry : loadedDefinitionPacks.entrySet()) {
            Version version = getVersionForPackName(entry.getKey());
            if(version != null) {
                try {
                    if(version.compare(targetVersion) <= 0) {
                        if (latestMatch == null || version.compare(latestMatch.getKey()) > 0) {
                            latestMatch = new AbstractMap.SimpleEntry<>(version, entry.getValue());
                        }
                    }
                } catch(UnsupportedOperationException ignore) {}
            }
        }

        Debug.log(loadedDefinitionPacks);
        Debug.log(latestMatch);

        if(latestMatch == null) {
            Debug.log("Couldn't find a definition pack for version '" + targetVersion + "'");
            Debug.log("Loaded definition packs: " + loadedDefinitionPacks);
        }

        return latestMatch != null ? new DefinitionPack[] {latestMatch.getValue()} : null;
    }

    public static JavaEditionVersion getLatestKnownJavaVersion() {
        return latestKnownJavaVersion;
    }

    public static BedrockEditionVersion getLatestKnownBedrockVersion() {
        return latestKnownBedrockVersion;
    }

    public static Map<String, DefinitionPack> getAliasMap() {
        return loadedDefinitionPacks;
    }
}
