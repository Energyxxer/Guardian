package com.energyxxer.guardian.ui.commodoreresources;

import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.global.ProcessManager;
import com.energyxxer.guardian.global.Resources;
import com.energyxxer.guardian.global.Status;
import com.energyxxer.guardian.global.temp.projects.ProjectManager;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.dialogs.OptionDialog;
import com.energyxxer.guardian.util.NetworkUtil;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.util.processes.AbstractProcess;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.bind.util.ISO8601Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;

public class DefinitionUpdateProcess extends AbstractProcess {

    private final Gson gson = new Gson();

    private boolean promptedUpdate = false;
    private String lastCheckedDefCommit = null;
    private Date latestCommitDate = null;

    public static Preferences.SettingPref<Boolean> CHECK_FOR_DEF_UPDATES_STARTUP = new Preferences.SettingPref<>("settings.behavior.check_def_updates_startup", true, Boolean::new);

    public DefinitionUpdateProcess() {
        super("Definition Update");
        initializeThread(this::checkForUpdates);
    }

    public static void tryUpdate() {
        ProcessManager.queueProcess(new DefinitionUpdateProcess());
    }

    public void checkForUpdates() {
        if(!Guardian.core.usesJavaEditionDefinitions() && !Guardian.core.usesBedrockEditionDefinitions()) {
            this.finalizeProcess(true);
            return; //Doesn't use any definitions
        }
        try {
            updateStatus("Checking for definition updates");
            JsonElement lastCheckedDefCommitElement = Resources.resources.get("last-checked-definition-commit");
            lastCheckedDefCommit = null;
            if(lastCheckedDefCommitElement != null) {
                lastCheckedDefCommit = lastCheckedDefCommitElement.getAsString();
            }

            String defPackCommitsURL = "https://api.github.com/repos/Energyxxer/Minecraft-Definitions/commits?path=defpacks/zipped/";
            if(lastCheckedDefCommit != null) defPackCommitsURL += "&since=" + lastCheckedDefCommit;

            String featMapCommitsURL = "https://api.github.com/repos/Energyxxer/Minecraft-Definitions/commits?path=featuremaps/";
            if(lastCheckedDefCommit != null) featMapCommitsURL += "&since=" + lastCheckedDefCommit;

            String typeMapCommitsURL = "https://api.github.com/repos/Energyxxer/Minecraft-Definitions/commits?path=typemaps/zipped/";
            if(lastCheckedDefCommit != null) typeMapCommitsURL += "&since=" + lastCheckedDefCommit;

            HashSet<String> defPackChanges = new HashSet<>();
            HashSet<String> featMapChanges = new HashSet<>();
            HashSet<String> typeMapChanges = new HashSet<>();

            scanCommitOverview(defPackCommitsURL, defPackChanges, "defpacks/zipped/minecraft_", ".zip");
            scanCommitOverview(featMapCommitsURL, featMapChanges, "featuremaps/", ".json");
            scanCommitOverview(typeMapCommitsURL, typeMapChanges, "typemaps/zipped/", ".zip");

            updateStatus("Updating definition packs");
            syncChanges(defPackChanges, "defpacks/zipped/minecraft_*.zip", "resources" + File.separator + "defpacks" + File.separator + "*.zip");
            updateStatus("Updating feature maps");
            syncChanges(featMapChanges, "featuremaps/*.json", "resources" + File.separator + "featmaps" + File.separator + "*.json");
            updateStatus("Updating type maps");
            syncChanges(typeMapChanges, "typemaps/zipped/*.zip", "resources" + File.separator + "typemaps" + File.separator + "*.zip");

            if(promptedUpdate) {
                DefinitionPacks.loadAll();
                VersionFeatureResources.loadAll();
                TypeMaps.loadAll();
                ProjectManager.loadWorkspace();

                Resources.resources.addProperty("last-checked-definition-commit", ISO8601Utils.format(latestCommitDate));
                Resources.saveAll();
            }

            updateStatus("All definitions up to date");
            this.finalizeProcess(true);
        } catch(UnknownHostException ignored) {
            Debug.log("Unable to check for updates: no internet connection");
        } catch(UpdateAbortException ignored) {
        } catch(IOException x) {
            if(x.getMessage().contains("API rate limit exceeded")) {
                GuardianWindow.showError("API rate limit exceeded, try again later");
                GuardianWindow.setStatus(new Status(Status.ERROR, "API rate limit exceeded, try again later"));
            }
            x.printStackTrace();
        } catch (Exception x) {
            x.printStackTrace();
        }
        updateStatus("");
        this.finalizeProcess(false);
    }

    private void scanCommitOverview(String commitListURL, HashSet<String> changeSet, String expectedPrefix, String expectedSuffix) throws ParseException, IOException {
        JsonArray defPackCommits = gson.fromJson(new InputStreamReader(NetworkUtil.retrieveStreamForURLAuth(commitListURL, false)), JsonArray.class);

        for(JsonElement commitOverviewRaw : defPackCommits) {
            JsonObject commitOverview = commitOverviewRaw.getAsJsonObject();

            String commitDateRaw = commitOverview.getAsJsonObject("commit").getAsJsonObject("committer").get("date").getAsString();
            if(Objects.equals(commitDateRaw, lastCheckedDefCommit)) continue;
            Date commitDate = ISO8601Utils.parse(commitDateRaw, new ParsePosition(0));
            if(latestCommitDate == null) {
                latestCommitDate = commitDate;
            } else if(commitDate.after(latestCommitDate)) {
                latestCommitDate = commitDate;
            }

            String commitDetailsURL = commitOverview.get("url").getAsString();

            JsonObject commitDetails = gson.fromJson(new InputStreamReader(NetworkUtil.retrieveStreamForURLAuth(commitDetailsURL, false)), JsonObject.class);
            JsonArray changedFiles = commitDetails.getAsJsonArray("files");
            for(JsonElement file : changedFiles) {
                boolean valid = false;
                String filename = file.getAsJsonObject().get("filename").getAsString();

                if(filename.startsWith(expectedPrefix) && filename.endsWith(expectedSuffix) &&
                        (filename.startsWith(expectedPrefix + "j_") && Guardian.core.usesJavaEditionDefinitions())
                        || (filename.startsWith(expectedPrefix + "b_") && Guardian.core.usesBedrockEditionDefinitions())
                ) {
                    Debug.log("CHANGED:" + filename);
                    filename = filename.substring(expectedPrefix.length(), filename.length() - expectedSuffix.length());
                    changeSet.add(filename);
                    valid = true;
                }
                String status = file.getAsJsonObject().get("status").getAsString();
                if("renamed".equals(status)) {
                    String previousFilename = file.getAsJsonObject().get("previous_filename").getAsString();

                    if(previousFilename.startsWith(expectedPrefix) && previousFilename.startsWith(expectedSuffix) &&
                            (filename.startsWith(expectedPrefix + "j_") && Guardian.core.usesJavaEditionDefinitions())
                            || (filename.startsWith(expectedPrefix + "b_") && Guardian.core.usesBedrockEditionDefinitions())
                    ) {
                        previousFilename = previousFilename.substring(expectedPrefix.length(), previousFilename.length() - expectedSuffix.length());
                        changeSet.add(previousFilename);
                        valid = true;
                    }
                }

                if(valid && !promptedUpdate) {
                    updateStatus("");
                    String confirmation = new OptionDialog("Minecraft Definitions", "There are updates to definitions available. Update them now?", new String[] {"Update", "Not now"}).result;
                    if(!"Update".equals(confirmation)) {
                        Debug.log("Aborting update process...");
                        throw new UpdateAbortException();
                    }
                    updateStatus("Retrieving updated files");
                    promptedUpdate = true;
                }
            }
        }
    }

    private void syncChanges(HashSet<String> changeSet, String repoPath, String destinationPath) throws IOException {
        for(String filename : changeSet) {
            InputStream is = NetworkUtil.retrieveStreamForURLAuth("https://raw.githubusercontent.com/Energyxxer/Minecraft-Definitions/master/" + repoPath.replace("*", filename), true);
            File targetFile = Guardian.core.getMainDirectory().resolve(destinationPath.replace("*", filename)).toFile();
            if(is == null) {
                // File deleted
                Debug.log("DELETE FILE " + targetFile);
                //targetFile.delete();
            } else {
                Debug.log("CREATE FILE " + targetFile);
                targetFile.getParentFile().mkdirs();
                targetFile.createNewFile();
                Files.copy(is, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                is.close();
            }
        }
    }

    public class UpdateAbortException extends RuntimeException {
    }
}
