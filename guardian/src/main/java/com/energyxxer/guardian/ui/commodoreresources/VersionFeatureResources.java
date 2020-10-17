package com.energyxxer.guardian.ui.commodoreresources;

import com.energyxxer.commodore.versioning.compatibility.VersionFeatureManager;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.util.logger.Debug;

import java.io.File;
import java.io.FileReader;

public class VersionFeatureResources {
    public static void loadAll() {
        VersionFeatureManager.clearLoadedFeatures();

        File featMapDir = Guardian.core.getFeatureMapsDir();
        featMapDir.mkdirs();
        File[] files = featMapDir.listFiles();
        if(files != null) {
            for(File file : files) {
                if(file.isFile() && file.getName().endsWith(".json")) {
                    try (FileReader fr = new FileReader(file)) {
                        VersionFeatureManager.loadFeatureMap(fr);
                    } catch (Exception e) {
                        Debug.log(e.getMessage(), Debug.MessageType.ERROR);
                    }
                }
            }
        }

        Debug.log("Loaded version feature maps");
    }
}
