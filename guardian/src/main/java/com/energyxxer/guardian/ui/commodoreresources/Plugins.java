package com.energyxxer.guardian.ui.commodoreresources;

import com.energyxxer.commodore.util.io.DirectoryCompoundInput;
import com.energyxxer.commodore.util.io.ZipCompoundInput;
import com.energyxxer.prismarine.PrismarineSuiteConfiguration;
import com.energyxxer.prismarine.plugins.PrismarinePlugin;
import com.energyxxer.util.logger.Debug;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Plugins {
    private static final ArrayList<GuardianPluginLoader> loaders = new ArrayList<>();

    private static HashMap<PrismarineSuiteConfiguration, LinkedHashMap<String, PrismarinePlugin>> loadedPlugins = new HashMap<>();

    public static void loadAll() {
        loadedPlugins.clear();

        for(GuardianPluginLoader loader : loaders) {
            File pluginsDir = loader.getPluginsDirectory();
            pluginsDir.mkdirs();

            PrismarineSuiteConfiguration suiteConfig = loader.getSuiteConfig();

            LinkedHashMap<String, PrismarinePlugin> pluginsForLoader = new LinkedHashMap<>();
            loadedPlugins.put(loader.getSuiteConfig(), pluginsForLoader);

            File[] files = pluginsDir.listFiles();
            if(files != null) {
                for(File file : files) {
                    PrismarinePlugin plugin = null;
                    if(file.isDirectory()) {
                        String packName = file.getName();
                        plugin = new PrismarinePlugin(packName, new DirectoryCompoundInput(file), file, suiteConfig);
                        pluginsForLoader.put(packName, plugin);
                    } else if(file.isFile() && file.getName().endsWith(".zip")) {
                        String packName = file.getName().substring(0, file.getName().length() - ".zip".length());
                        plugin = new PrismarinePlugin(packName, new ZipCompoundInput(file), file, suiteConfig);
                        pluginsForLoader.put(packName, plugin);
                    }
                    try {
                        if(plugin != null) plugin.load();
                    } catch(Exception e) {
                        Debug.log(e.getMessage(), Debug.MessageType.ERROR);
                    }
                }
            }
        }

        Debug.log("Loaded plugins");
    }

    public static Map<String, PrismarinePlugin> getAliasMap(PrismarineSuiteConfiguration suiteConfiguration) {
        return loadedPlugins.get(suiteConfiguration);
    }

    public static void registerPluginLoader(GuardianPluginLoader loader) {
        loaders.add(loader);
    }
}
