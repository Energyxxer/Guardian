package com.energyxxer.guardian.ui.modules;

import com.energyxxer.guardian.files.FileType;
import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.global.FileManager;
import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.global.temp.Lang;
import com.energyxxer.guardian.global.temp.projects.Project;
import com.energyxxer.guardian.global.temp.projects.ProjectManager;
import com.energyxxer.guardian.langinterface.ProjectType;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.common.MenuItems;
import com.energyxxer.guardian.ui.dialogs.PromptDialog;
import com.energyxxer.guardian.ui.dialogs.file_dialogs.ProjectDialog;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.editor.EditorModule;
import com.energyxxer.guardian.ui.explorer.ProjectExplorerMaster;
import com.energyxxer.guardian.ui.imageviewer.ImageViewer;
import com.energyxxer.guardian.ui.styledcomponents.StyledMenu;
import com.energyxxer.guardian.ui.styledcomponents.StyledMenuItem;
import com.energyxxer.guardian.ui.styledcomponents.StyledPopupMenu;
import com.energyxxer.guardian.util.FileCommons;
import com.energyxxer.util.Lazy;
import com.energyxxer.util.logger.Debug;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FileModuleToken implements ModuleToken, DraggableExplorerModuleToken, DropTargetExplorerModuleToken {
    public static ModuleTokenFactory<FileModuleToken> factory = str -> {
        if(!str.startsWith("file://")) return null;
        String path = str.substring("file://".length());
        File file = new File(path);
        return file.exists() ? new FileModuleToken(file) : null;
    };

    private static final int MAX_RECENT_FILES = 16;
    public static final ArrayList<File> recentFiles = new ArrayList<>(MAX_RECENT_FILES);

    public static final Preferences.SettingPref<Boolean> SHOW_EXTENSIONS_EXPLORER = new Preferences.SettingPref<>("settings.behavior.hide_extensions.explorer", true, Boolean::parseBoolean);
    public static final Preferences.SettingPref<Boolean> SHOW_EXTENSIONS_TAB = new Preferences.SettingPref<>("settings.behavior.hide_extensions.tab", true, Boolean::parseBoolean);
    public static final Preferences.SettingPref<Boolean> LOAD_PNGS = new Preferences.SettingPref<>("settings.behavior.load_pngs", true, Boolean::parseBoolean);
    public static final Preferences.SettingPref<Boolean> DELETE_MOVES_TO_TRASH = new Preferences.SettingPref<Boolean>("settings.behavior.delete_moves_to_trash", true, Boolean::parseBoolean) {
        @Override
        public Boolean get() {
            return MOVE_TO_TRASH_VERSION_AVAILABLE && super.get();
        }
    };
    public static final boolean MOVE_TO_TRASH_VERSION_AVAILABLE = !System.getProperty("java.version").startsWith("1.8.");

    private final File file;
    private boolean isProjectRoot;
    private String overrideIconName = null;

    private File associatedProjectRoot;
    private Lazy<String> subTitle = new Lazy<>(() -> {
        String subTitle;
        File file = this.getFile();
        Project associatedProject = ProjectManager.getAssociatedProject(file);
        if(associatedProject != null) {
            this.associatedProjectRoot = associatedProject.getRootDirectory();
            if(file.equals(associatedProjectRoot)) {
                subTitle = "";
            } else {
                subTitle = associatedProjectRoot.toPath().relativize(file.getParentFile().toPath()).toString();
            }
            if(subTitle.isEmpty()) subTitle = null;
            else {
                subTitle = "(" + subTitle + ")";
            }
        } else {
            if(file.getParentFile() != null) {
                subTitle = "(" + file.getParentFile().toPath().toString() + ")";
            } else {
                subTitle = "";
            }
        }
        return subTitle;
    });

    public FileModuleToken(File file) {
        this.file = file;

        this.isProjectRoot = ProjectType.isAnyProjectRoot(file);
    }

    @Override
    public String getTitle(TokenContext context) {
        if((context == TokenContext.EXPLORER && SHOW_EXTENSIONS_EXPLORER.get()) || (context == TokenContext.TAB && SHOW_EXTENSIONS_TAB.get())) {
            if(file.getParentFile() == null) {
                String name = file.getPath();
                if(name.endsWith(File.separator)) {
                    name = name.substring(0, name.length()-1);
                }
                return name;
            } else {
                return file.getName();
            }
        } else {
            return getNameWithoutExtension();
        }
    }

    private String getNameWithoutExtension() {
        String name = file.getName();
        if(name.lastIndexOf('.') <= 0) return name;
        return name.substring(0, name.lastIndexOf('.'));
    }

    @Override
    public String getSubTitle() {
        return subTitle.getValue();
    }

    @Override
    public File getAssociatedProjectRoot() {
        return associatedProjectRoot;
    }

    @Override
    public Project getAssociatedProject() {
        return ProjectManager.getAssociatedProject(file);
    }

    @Override
    public float getAlpha() {
        return Commons.isProjectFile(file) ? 0.6f : 1f;
    }

    private boolean hideFolderIcon = false;

    public void setHideFolderIcon(boolean hideFolderIcon) {
        this.hideFolderIcon = hideFolderIcon;
    }

    @Override
    public Image getIcon() {
        if(overrideIconName != null) return Commons.getIcon(overrideIconName);
        Project associatedProject = ProjectManager.getAssociatedProject(file);
        if(associatedProject != null) {
            Image iconFromProject = associatedProject.getIconForFile(file);
            if(iconFromProject != null) return iconFromProject;
        }
        Lang associatedLang = Lang.getLangForFile(file.getPath());
        if(associatedLang != null) {
            Image iconFromLang = associatedLang.getIconForFile(file);
            if(iconFromLang != null) return iconFromLang;
        }

        if(file.isDirectory()) {
            if(isProjectRoot) {
                if(!ProjectManager.isLoadedProjectRoot(file)) {
                    return Commons.getIcon("project_unloaded");
                }
                ProjectType projectType = ProjectType.getProjectTypeForRoot(file);
                if(projectType != null) {
                    return projectType.getIconForRoot(file);
                }
            }
            return hideFolderIcon ? null : Commons.getIcon("folder");
        } else {
            String extension = "";
            if(file.getName().lastIndexOf(".") >= 0) {
                extension = file.getName().substring(file.getName().lastIndexOf("."));
            }
            if(extension.equals(".png") && LOAD_PNGS.get()) {
                try {
                    return ImageIO.read(file);
                } catch(IOException x) {
                    Debug.log("Couldn't load image from file '" + file + "'");
                    return Commons.getIcon("warn");
                }
            }
            switch(extension) {
                case ".mp3":
                case ".ogg":
                case ".fsb":
                    return Commons.getIcon("audio");
                case ".json": {
                    return Commons.getIcon("json");
                }
                case ".png":
                case ".tga":
                    return Commons.getIcon("image");
                default: return Commons.getIcon("file");
            }
        }
    }

    @Override
    public String getHint() {
        return file.getPath();
    }

    @Override
    public Collection<ModuleToken> getSubTokens() {
        Project associatedProject = ProjectManager.getAssociatedProject(file);

        ArrayList<ModuleToken> children = new ArrayList<>();
        int firstFileIndex = 0;
        File[] subFiles = file.listFiles();
        if(subFiles != null) {
            for (File subFile : subFiles) {
                FileModuleToken subToken = new FileModuleToken(subFile);
                if(GuardianWindow.projectExplorer.getFlag(ProjectExplorerMaster.SHOW_PROJECT_FILES) || !Commons.isProjectFile(subFile)) {
                    if(subFile.isDirectory()) {
                        children.add(firstFileIndex, subToken);
                        firstFileIndex++;
                    } else {
                        children.add(subToken);
                    }
                }
            }
        }
        return children;
    }

    @Override
    public boolean isExpandable() {
        return file.isDirectory();
    }

    @Override
    public DisplayModule createModule(Tab tab) {
        if(file.isFile()) {
            String name = file.getName();
            if(name.endsWith(".png")) {
                addRecentFile(file);
                return new ImageViewer(file);
            } else if(name.endsWith(".ogg") || name.endsWith(".mp3")) {

            } else {
                addRecentFile(file);
                return new EditorModule(tab, file);
            }
        }
        return null;
    }

    @Override
    public boolean isModuleSource() {
        return file.isFile();
    }

    private static void addRecentFile(File file) {
        recentFiles.remove(file);
        recentFiles.add(0, file);
        while(recentFiles.size() >= MAX_RECENT_FILES) {
            recentFiles.remove(MAX_RECENT_FILES-1);
        }
    }

    @Override
    public StyledPopupMenu generateMenu(@NotNull ModuleToken.TokenContext context) {
        StyledPopupMenu menu = new StyledPopupMenu();

        String path = getPath();

        String newPath;
        if(file.isDirectory()) newPath = path;
        else newPath = file.getParent();

        if(context == TokenContext.EXPLORER) {
            StyledMenu newMenu = new StyledMenu("New");

            menu.add(newMenu);

            Project project = ProjectManager.getAssociatedProject(file);
            String projectDir = (project != null) ? project.getRootDirectory().getPath() + File.separator : null;

            // --------------------------------------------------


            boolean anyMenuItem = false;
            {
                for(ProjectType projectType : ProjectType.values()) {
                    anyMenuItem = true;
                    StyledMenuItem item = new StyledMenuItem(projectType.getName(), projectType.getDefaultProjectIconName());
                    item.addActionListener(e -> {
                        ProjectDialog.create(projectType);
                    });
                    newMenu.add(item);
                }
            }

            // --------------------------------------------------

            if(anyMenuItem) newMenu.addSeparator();

            // --------------------------------------------------

            int prevGroup = 0;
            for(FileType type : FileType.values()) {
                if(type.canCreate(projectDir, path + File.separator)) {
                    if(type.group != prevGroup) {
                        if(anyMenuItem) newMenu.addSeparator();
                        prevGroup = type.group;
                    }

                    newMenu.add(type.createMenuItem(newPath));
                    anyMenuItem = true;
                }
            }
        }
        menu.addSeparator();


        if(context == TokenContext.EXPLORER) {
            List<ModuleToken> selectedTokens = GuardianWindow.projectExplorer.getSelectedTokens();
            ArrayList<FileModuleToken> selectedFileTokens = new ArrayList<>();
            ArrayList<File> selectedFiles = new ArrayList<>();
            selectedFileTokens.add(this);
            for(ModuleToken token : selectedTokens) {
                if(token instanceof FileModuleToken) {
                    if(token != this) selectedFileTokens.add((FileModuleToken) token);
                    selectedFiles.add(((FileModuleToken) token).getFile());
                }
            }

            ArrayList<String> selectedPaths = new ArrayList<>();
            for(FileModuleToken file : selectedFileTokens) {
                selectedPaths.add(file.getPath());
            }

            StyledMenuItem copyItem = MenuItems.fileItem(MenuItems.FileMenuItem.COPY);
            copyItem.addActionListener(e -> {
                Clipboard clipboard = menu.getToolkit().getSystemClipboard();
                clipboard.setContents(new Transferable() {
                    @Override
                    public DataFlavor[] getTransferDataFlavors() {
                        return new DataFlavor[] {DataFlavor.javaFileListFlavor, DataFlavor.stringFlavor};
                    }

                    @Override
                    public boolean isDataFlavorSupported(DataFlavor flavor) {
                        return flavor == DataFlavor.stringFlavor || flavor == DataFlavor.javaFileListFlavor;
                    }

                    @NotNull
                    @Override
                    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
                        if(flavor == DataFlavor.stringFlavor) {
                            StringBuilder sb = new StringBuilder();
                            for(File file : selectedFiles) {
                                sb.append(file);
                                sb.append('\n');
                            }
                            if(sb.length() > 0) sb.setLength(sb.length()-1);
                            return sb.toString();
                        }
                        if(flavor == DataFlavor.javaFileListFlavor) return selectedFiles;
                        throw new UnsupportedFlavorException(flavor);
                    }
                }, null);
            });
            menu.add(copyItem);

            StyledMenuItem pasteItem = MenuItems.fileItem(MenuItems.FileMenuItem.PASTE);
            pasteItem.addActionListener(e -> {
                try {
                    Clipboard clipboard = menu.getToolkit().getSystemClipboard();
                    if (clipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
                        //noinspection unchecked
                        File[] filesToCopy = ((List<File>) clipboard.getData(DataFlavor.javaFileListFlavor)).toArray(new File[0]);
                        for (File destination : selectedFiles) {
                            if (destination.isFile()) destination = destination.getParentFile();
                            FileCommons.copyFiles(filesToCopy, destination);
                            GuardianWindow.projectExplorer.refresh();
                        }
                    }
                } catch (UnsupportedFlavorException | IOException x) {
                    x.printStackTrace();
                }
            });
            menu.add(pasteItem);


            menu.addSeparator();

            StyledMenuItem renameItem = MenuItems.fileItem(MenuItems.FileMenuItem.RENAME);
            renameItem.addActionListener(e -> {
                if(selectedFileTokens.size() != 1) return;
                this.showRenameDialog();
            });
            menu.add(renameItem);



            StyledMenuItem deleteItem = MenuItems.fileItem(MenuItems.FileMenuItem.DELETE);
            deleteItem.setEnabled(selectedFileTokens.size() >= 1);
            deleteItem.addActionListener(e -> FileManager.delete(selectedPaths));
            menu.add(deleteItem);

            menu.addSeparator();
        }


        StyledMenuItem openInSystemItem = new StyledMenuItem("Show in System Explorer", "explorer");
        openInSystemItem.addActionListener(e -> Commons.showInSystemExplorer(path));

        menu.add(openInSystemItem);

        return menu;
    }

    public void showRenameDialog() {
        String pathToRename = this.getPath();
        String name = new File(pathToRename).getName();
        String rawName = name.substring(0, name.contains(".") ? name.lastIndexOf(".") : name.length());
        final String pathToParent = pathToRename.substring(0, pathToRename.lastIndexOf(name));

        String newName = new PromptDialog("Rename", "Enter a new name for the file:", name) {
            @Override
            protected boolean validate(String str) {
                return str.trim().length() > 0 && FileCommons.validateFilename(str)
                        && !new File(pathToParent + str).exists();
            }

            @Override
            protected int getSelectionEnd() {
                return rawName.length();
            }
        }.result;

        if (newName != null) {
            if (ProjectManager.renameFile(new File(pathToRename), newName)) {
                GuardianWindow.projectExplorer.refresh();
                GuardianWindow.tabManager.openTabs.forEach(
                        tab -> {
                            if(tab.token instanceof FileModuleToken && ((FileModuleToken) tab.token).getFile().equals(this.getFile())) {
                                tab.transform(new FileModuleToken(new File(pathToParent + newName)));
                            }
                        }
                );
                GuardianWindow.tabManager.saveOpenTabs();
                GuardianWindow.tabList.repaint();
            } else {
                JOptionPane.showMessageDialog(null,
                        "<html>The action can't be completed because the folder or file is open in another program.<br>Close the folder and try again.</html>",
                        "An error occurred.", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public File getFile() {
        return file;
    }

    public String getPath() {
        return file.getPath();
    }

    @Override
    public String getIdentifier() {
        return "file://" + getPath();
    }

    @Override
    public boolean equals(ModuleToken other) {
        return other instanceof FileModuleToken && ((FileModuleToken) other).file.equals(this.file);
    }

    @Override
    public String toString() {
        return "[FileModuleToken: " + getPath() + "]";
    }

    public File getDragDestination() {
        File destination = getFile().isDirectory() ? getFile() : getFile().getParentFile();
        return destination != null && destination.exists() ? destination : null;
    }

    @Override
    public boolean canAcceptMove(DraggableExplorerModuleToken[] draggables) {
        File destination = getDragDestination();
        if(destination == null) return false;
        for(DraggableExplorerModuleToken draggable : draggables) {
            if(!(draggable instanceof FileModuleToken)) return false;
            if(destination.equals(((FileModuleToken) draggable).file.getParentFile())) return false;
            if(destination.toPath().startsWith(((FileModuleToken) draggable).file.toPath())) return false;
        }
        return true;
    }

    @Override
    public boolean canAcceptCopy(DraggableExplorerModuleToken[] draggables) {
        File destination = getDragDestination();
        if(destination == null) return false;
        for(DraggableExplorerModuleToken draggable : draggables) {
            if(!(draggable instanceof FileModuleToken)) return false;
            if(destination.toPath().startsWith(((FileModuleToken) draggable).file.toPath())) return false;
        }
        return true;
    }

    @Override
    public DataFlavor getDataFlavor() {
        return DataFlavor.javaFileListFlavor;
    }

    @Override
    public File getTransferData() {
        return file;
    }
}
