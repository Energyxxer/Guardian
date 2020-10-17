package com.energyxxer.guardian.ui.commodoreresources;

import com.energyxxer.prismarine.PrismarineSuiteConfiguration;

import java.io.File;

public interface GuardianPluginLoader {
    PrismarineSuiteConfiguration getSuiteConfig();
    File getPluginsDirectory();
}
