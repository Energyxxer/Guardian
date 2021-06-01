package com.energyxxer.guardian.ui.common;

import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.global.ProcessManager;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.actions.ActionManager;
import com.energyxxer.guardian.ui.commodoreresources.DefinitionUpdateProcess;
import com.energyxxer.guardian.ui.dialogs.OptionDialog;
import com.energyxxer.guardian.util.NetworkUtil;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.util.processes.AbstractProcess;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static com.energyxxer.guardian.util.NetworkUtil.retrieveStreamForURL;
import static com.energyxxer.guardian.util.NetworkUtil.retrieveStreamForURLAuth;

public class ProgramUpdateProcess extends AbstractProcess {
    public static Preferences.SettingPref<Boolean> CHECK_FOR_PROGRAM_UPDATES_STARTUP = new Preferences.SettingPref<>("settings.behavior.check_program_updates_startup", true, Boolean::parseBoolean);

    public ProgramUpdateProcess() {
        super("Program Update");
        initializeThread(this::checkForUpdates);
    }

    public static void tryUpdate() {
        ProcessManager.queueProcess(new ProgramUpdateProcess());
    }

    public void checkForUpdates() {
        try {
            updateStatus("Checking for program updates");

            ProgramVersionInfo updateAvailable = Guardian.core.checkForUpdates();
            if(updateAvailable != null) {
                updateStatus("Newer version found: " + updateAvailable.name);
                updateProgress(0);

                boolean runningJar = Guardian.RUNNING_PATH != null && Guardian.RUNNING_PATH.isFile() && Guardian.RUNNING_PATH.getName().endsWith(".jar");
                String[] options = runningJar ? new String[] {"View changelog", "Replace my .jar", "Not now"} : new String[] {"View changelog", "Not now"};

                OptionDialog dialog = new OptionDialog("Program Update", "A new version of " + Guardian.core.getProgramName() + " is available. Update now?", options);
                if("View changelog".equals(dialog.result)) {
                    try {
                        Desktop.getDesktop().browse(new URI(updateAvailable.htmlUrl));
                    } catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                } else if("Replace my .jar".equals(dialog.result)) {
                    Path newJar = Guardian.RUNNING_PATH.getParentFile().toPath().resolve(updateAvailable.jarName);
                    Preferences.put("meta.delete_old_jar", Guardian.RUNNING_PATH.getPath());

                    updateStatusAndProgress("Downloading new JAR", -1);

                    InputStream is;
                    if(updateAvailable.authOnly) {
                        is = retrieveStreamForURL(NetworkUtil.AUTHENTICATED_REQUEST_API + updateAvailable.downloadUrl + "&custom_header=accept:application/octet-stream", false);
                    } else {
                        is = retrieveStreamForURLAuth(updateAvailable.downloadUrl, false);
                    }
                    Files.copy(is, newJar, StandardCopyOption.REPLACE_EXISTING);
                    is.close();

                    GuardianWindow.setRestartingJar(newJar.toFile());
                    ActionManager.getAction("EXIT").perform();

                }
            }
            updateStatus("");
            this.finalizeProcess(true);
            return;
        } catch(UnknownHostException ignored) {
            Debug.log("Unable to check for updates: no internet connection");
        } catch(DefinitionUpdateProcess.UpdateAbortException ignored) {
        } catch (Exception x) {
            x.printStackTrace();
        }
        updateStatus("");
        this.finalizeProcess(false);
    }

    public static class ProgramVersionInfo {
        public String name;
        public String jarName;
        public String htmlUrl;
        public String downloadUrl;
        public boolean authOnly;

        public ProgramVersionInfo(String name, String jarName, String htmlUrl, String downloadUrl) {
            this.name = name;
            this.jarName = jarName;
            this.htmlUrl = htmlUrl;
            this.downloadUrl = downloadUrl;
        }

        public ProgramVersionInfo(String name, String jarName, String htmlUrl, String downloadUrl, boolean authOnly) {
            this.name = name;
            this.jarName = jarName;
            this.htmlUrl = htmlUrl;
            this.downloadUrl = downloadUrl;
            this.authOnly = authOnly;
        }
    }
}
