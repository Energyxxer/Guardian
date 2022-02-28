package com.energyxxer.guardian.main.window.actions;

import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.global.FileManager;
import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.global.Resources;
import com.energyxxer.guardian.global.keystrokes.KeyMap;
import com.energyxxer.guardian.global.keystrokes.SpecialMapping;
import com.energyxxer.guardian.global.keystrokes.UserKeyBind;
import com.energyxxer.guardian.global.keystrokes.UserMapping;
import com.energyxxer.guardian.global.temp.projects.Project;
import com.energyxxer.guardian.global.temp.projects.ProjectManager;
import com.energyxxer.guardian.langinterface.ProjectType;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.main.WorkspaceDialog;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.sections.AboutPane;
import com.energyxxer.guardian.main.window.sections.quick_find.QuickFindDialog;
import com.energyxxer.guardian.main.window.sections.search_path.SearchPathDialog;
import com.energyxxer.guardian.main.window.sections.tools.ConsoleBoard;
import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.commodoreresources.DefinitionUpdateProcess;
import com.energyxxer.guardian.ui.commodoreresources.Plugins;
import com.energyxxer.guardian.ui.common.ProgramUpdateProcess;
import com.energyxxer.guardian.ui.dialogs.KeyStrokeDialog;
import com.energyxxer.guardian.ui.dialogs.file_dialogs.ProjectDialog;
import com.energyxxer.guardian.ui.dialogs.settings.Settings;
import com.energyxxer.guardian.ui.tablist.TabItem;
import com.energyxxer.util.logger.Debug;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class ActionManager {
    private static LinkedHashMap<String, ProgramAction> actions = new LinkedHashMap<>();

    static {
        actions.put("COMPILE",
                new ProgramAction(
                        "Compile", "Compile the active project", 
                        KeyMap.requestMapping("compile", KeyMap.identifierToStrokes("as+X")).setGroupName("Projects"),
                        Commons::compileActive
                ).setIconKey("export")
        );
        actions.put("CLOSE_TAB",
                new ProgramAction(
                    "Close Active Tab", "Close the tab currently visible",
                    KeyMap.requestMapping("tab.close", KeyMap.identifierToStrokes("c+W")).setGroupName("Tabs"),
                    GuardianWindow.tabManager::closeSelectedTab
                )
        );
        actions.put("CLOSE_ALL_TABS",
                new ProgramAction(
                        "Close All Tabs", "Close all tabs",
                        KeyMap.requestMapping("tab.close_all", KeyMap.identifierToStrokes("cs+W")).setGroupName("Tabs"),
                        () -> GuardianWindow.tabManager.closeAllTabs(false)
                )
        );
        actions.put("CLOSE_ALL_TABS_FOR_PROJECT",
                new ProgramAction(
                        "Close All Tabs For Project", "Close all tabs",
                        KeyMap.requestMapping("tab.close_project").setGroupName("Tabs"),
                        () -> GuardianWindow.tabManager.closeAllTabsForProject(Commons.getActiveProject())
                )
        );
        actions.put("PREVIOUS_TAB",
                new ProgramAction(
                        "Previous Tab", "Switch to the previous tab",
                        KeyMap.requestMapping("tab.previous", KeyMap.identifierToStrokes("cs+\t")).setGroupName("Tabs"),
                        () -> {
                            Tab selectedTab = GuardianWindow.tabManager.getSelectedTab();
                            if(selectedTab != null) {
                                int index = GuardianWindow.tabList.getTabItems().indexOf(selectedTab.getLinkedTabItem());
                                int startIndex = index;
                                do {
                                    index--;
                                    if(index < 0) {
                                        index = GuardianWindow.tabList.getTabItems().size() - 1;
                                    }
                                } while(!(GuardianWindow.tabList.getTabItems().get(index) instanceof TabItem) && index != startIndex);
                                GuardianWindow.tabManager.setSelectedTab(((TabItem) GuardianWindow.tabList.getTabItems().get(index)).getAssociatedTab());
                            }
                        }
                )
        );
        actions.put("NEXT_TAB",
                new ProgramAction(
                        "Next Tab", "Switch to the next tab",
                        KeyMap.requestMapping("tab.next", KeyMap.identifierToStrokes("c+\t")).setGroupName("Tabs"),
                        () -> {
                            Tab selectedTab = GuardianWindow.tabManager.getSelectedTab();
                            if(selectedTab != null) {
                                int index = GuardianWindow.tabList.getTabItems().indexOf(selectedTab.getLinkedTabItem());
                                int startIndex = index;
                                do {
                                    index++;
                                    if(index >= GuardianWindow.tabList.getTabItems().size()) {
                                        index = 0;
                                    }
                                } while(!(GuardianWindow.tabList.getTabItems().get(index) instanceof TabItem) && index != startIndex);
                                GuardianWindow.tabManager.setSelectedTab(((TabItem) GuardianWindow.tabList.getTabItems().get(index)).getAssociatedTab());
                            }
                        }
                )
        );
        actions.put("SAVE",
                new ProgramAction(
                "Save Active Tab", "Save the tab currently visible",
                    KeyMap.requestMapping("tab.save", KeyMap.identifierToStrokes("c+S")).setGroupName("Tabs"),
                    () -> {
                        Tab st = GuardianWindow.tabManager.getSelectedTab();
                        if(st != null) st.save();
                    }
                ).setIconKey("save")
        );
        actions.put("SAVE_ALL",
                new ProgramAction(
                    "Save All Tabs", "Save all open tabs",
                    KeyMap.requestMapping("tab.save_all", KeyMap.identifierToStrokes("ca+S")).setGroupName("Tabs"),
                    () -> {
                        for(Tab st : GuardianWindow.tabManager.openTabs) {
                            st.save();
                        }
                        Debug.log("All tabs saved");
                    }
                ).setIconKey("save_all")
        );
        actions.put("RELOAD_FROM_DISK",
                new ProgramAction(
                    "Reload from Disk", "Reload the current file from disk",
                    KeyMap.requestMapping("editor.reload").setGroupName("Editor"),
                    "editor.reload"
                ).setIconKey("reload")
        );
        actions.put("RELOAD_THEME",
                new ProgramAction(
                    "Reload GUI Resources", "Reload themes, definition packs and feature maps from disk",
                    KeyMap.requestMapping("theme.reload", KeyMap.identifierToStrokes("c+T")),
                    Resources::load
                ).setIconKey("reload")
        );
        actions.put("JUMP_TO_MATCHING_BRACE",
                new ProgramAction(
                        "Jump to Matching Brace", "Set caret position to the selected brace's match",
                        KeyMap.requestMapping("editor.jump_to_matching_brace", KeyMap.identifierToStrokes("cs+P")).setGroupName("Editor"),
                        "editor.jump_to_matching_brace"
                )
        );
        actions.put("EDITOR_FIND",
                new ProgramAction(
                    "Find in Editor", "Find all occurrences of a query in the current editor tab",
                    KeyMap.requestMapping("editor.find", KeyMap.identifierToStrokes("c+F")).setGroupName("Editor"),
                    "editor.find"
                ).setIconKey("search")
        );
        actions.put("FIND_IN_PATH",
                new ProgramAction(
                    "Find in Path", "Find all occurrences of a query in a folder or project",
                    KeyMap.requestMapping("find_in_path", KeyMap.identifierToStrokes("c+H")).setGroupName("Windows"),
                    SearchPathDialog.INSTANCE::reveal
                ).setIconKey("search")
        );
        actions.put("SEARCH_EVERYWHERE",
                new ProgramAction(
                    "Search Everywhere", "Search for files and actions",
                    KeyMap.requestMapping("quick_access", KeyMap.identifierToStrokes("cs+E;"+ UserKeyBind.Special.DOUBLE_SHIFT.getIdentifier())).setGroupName("Windows"),
                    QuickFindDialog.INSTANCE::reveal
                ).setIconKey("search")
        );
        actions.put("PROJECT_PROPERTIES",
                new ProgramAction(
                    "Project Properties", "Edit the current project",
                    KeyMap.requestMapping("open_project_properties", KeyMap.identifierToStrokes("sa+S")).setGroupName("Windows"),
                    () -> {
                        Project selectedProject = Commons.getActiveProject();
                        if(selectedProject != null) {
                            selectedProject.getProjectType().showProjectPropertiesDialog(selectedProject);
                        } else {
                            GuardianWindow.showPopupMessage("No project selected");
                        }
                    }
                ).setIconKey("project_properties")
        );
        actions.put("CHECK_FOR_UPDATES",
                new ProgramAction(
                        "Check for Definition Updates", "Check for definition updates",
                        KeyMap.requestMapping("update_check"),
                        DefinitionUpdateProcess::tryUpdate
                )
        );
        actions.put("CHECK_FOR_PROGRAM_UPDATES",
                new ProgramAction(
                        "Check for Program Updates", "Check for program and language updates",
                        KeyMap.requestMapping("program_update_check"),
                        ProgramUpdateProcess::tryUpdate
                )
        );
        actions.put("CHANGE_WORKSPACE",
                new ProgramAction(
                        "Select Workspace", "Select a directory to put your projects in",
                        KeyMap.requestMapping("change_workspace").setGroupName("Projects"),
                        WorkspaceDialog::prompt
                ).setIconKey("folder")
        );
        actions.put("CLEAR_WORKSPACE_HISTORY",
                new ProgramAction(
                        "Clear Workspace History", "Clear the list of previously used workspaces",
                        KeyMap.requestMapping("clear_workspace_history").setGroupName("Projects"),
                        () -> {
                            File currentWorkspace = Preferences.getWorkspace();
                            Preferences.WORKSPACE_HISTORY.clear();
                            Preferences.setWorkspace(currentWorkspace);
                        }
                )
        );
        actions.put("RELOAD_WORKSPACE",
                new ProgramAction(
                        "Reload Workspace", "Refresh the list of projects",
                        KeyMap.requestMapping("reload_workspace", KeyMap.identifierToStrokes("" + KeyEvent.VK_F5)).setGroupName("Projects"),
                        () -> {
                            ProjectManager.loadWorkspace();
                            GuardianWindow.projectExplorer.refresh();
                            Plugins.loadAll();
                        }
                ).setIconKey("reload")
        );
        actions.put("CLEAR_RESOURCE_CACHE",
                new ProgramAction(
                        "Clear Project Resource Cache", "Force the entire resource pack to be exported on next compilation",
                        KeyMap.requestMapping("clear_resource_cache").setGroupName("Projects"),
                        () -> {
                            Project project = Commons.getActiveProject();
                            if(project != null) {
                                project.clearPersistentCache();
                            }
                        }
                ).setIconKey("reload")
        );
        actions.put("UNDO",
                new ProgramAction(
                    "Undo", "Undo the last change",
                    KeyMap.UNDO,
                    "undo"
                ).setUsableFunction(ProgramAction.USABLE_NOWHERE).setIconKey("undo")
        );
        actions.put("REDO",
                new ProgramAction(
                    "Redo", "Redo the last change undone",
                    KeyMap.REDO,
                    "redo"
                ).setUsableFunction(ProgramAction.USABLE_NOWHERE).setIconKey("redo")
        );
        actions.put("COPY",
                new ProgramAction(
                    "Copy", "Copy selected text",
                    KeyMap.COPY,
                    "copy"
                ).setUsableFunction(ProgramAction.USABLE_NOWHERE)
        );
        actions.put("CUT",
                new ProgramAction(
                    "Cut", "Cut selected text",
                    KeyMap.CUT,
                    "cut"
                ).setUsableFunction(ProgramAction.USABLE_NOWHERE)
        );
        actions.put("PASTE",
                new ProgramAction(
                    "Paste", "Paste text from clipboard",
                    KeyMap.PASTE,
                    "paste"
                ).setUsableFunction(ProgramAction.USABLE_NOWHERE)
        );
        actions.put("DELETE",
                new ProgramAction(
                    "Delete", "Delete selected text",
                    KeyMap.requestMapping("delete", KeyMap.identifierToStrokes(KeyEvent.VK_DELETE + ";" + KeyEvent.VK_BACK_SPACE)).setGroupName("Editor"),
                    "delete"
                ).setUsableFunction(ProgramAction.USABLE_NOWHERE)
        );
        actions.put("SHOW_HINTS",
                new ProgramAction(
                        "Show Hints", "Show hints for currently selected text",
                        KeyMap.requestMapping("show_hints", KeyMap.identifierToStrokes("a+" + KeyEvent.VK_ENTER)).setGroupName("Editor"),
                        "show_hints"
                ).setUsableFunction(ProgramAction.USABLE_IN_EDITOR)
        );
        for(ProjectType type : ProjectType.values()) {
            actions.put("NEW_PROJECT_" + type.getCode(),
                    new ProgramAction(
                            "New " + type.getName(), "Create new " + type.getName(),
                            KeyMap.requestMapping("new_project_" + type.getCode().toLowerCase(Locale.ENGLISH)).setGroupName("Projects"),
                            () -> ProjectDialog.create(type)
                    ).setIconKey(type.getDefaultProjectIconName())
            );
        }
        actions.put("SETTINGS",
                new ProgramAction(
                        "Settings", "Configure " + Guardian.core.getProgramName(),
                        KeyMap.requestMapping("open_settings", KeyMap.identifierToStrokes("csa+S")).setGroupName("Windows"),
                        Settings::show
                ).setIconKey("cog")
        );
        actions.put("DOCUMENTATION",
                new ProgramAction(
                        "Documentation", "Open the language documentation",
                        KeyMap.requestMapping("open_documentation", KeyMap.identifierToStrokes("" + KeyEvent.VK_F1)),
                        () -> {
                            try {
                                URI uri = Guardian.core.getDocumentationURI();
                                if(uri != null) Desktop.getDesktop().browse(uri);
                            } catch (IOException | URISyntaxException ex) {
                                ex.printStackTrace();
                            }
                        }
                ).setIconKey("documentation")
        );
        actions.put("TOGGLE_TOOL_BOARD",
                new ProgramAction(
                        "Toggle Tool Board", "Open/Close Tool Boards",
                        KeyMap.requestMapping("toggle_tool_board", KeyMap.identifierToStrokes("c+" + KeyEvent.VK_BACK_QUOTE + ";c+" + KeyEvent.VK_NUMPAD0)).setGroupName("Windows"),
                        () -> GuardianWindow.toolBoard.toggle()
                )
        );
        actions.put("OPEN_TODO",
                new ProgramAction(
                        "Show TODO", "Open TODO Board",
                        KeyMap.requestMapping("open_tool_board_todo", KeyMap.identifierToStrokes("c+" + KeyEvent.VK_1 + ";c+" + KeyEvent.VK_NUMPAD1)).setGroupName("Windows"),
                        () -> GuardianWindow.todoBoard.open()
                ).setIconKey("todo")
        );
        actions.put("OPEN_NOTICE_BOARD",
                new ProgramAction(
                        "Show Notices", "Open Notice Board",
                        KeyMap.requestMapping("open_tool_board_notices", KeyMap.identifierToStrokes("c+" + KeyEvent.VK_2 + ";c+" + KeyEvent.VK_NUMPAD2)).setGroupName("Windows"),
                        () -> GuardianWindow.noticeBoard.open()
                ).setIconKey("notices")
        );
        actions.put("OPEN_CONSOLE",
                new ProgramAction(
                        "Show Console", "Open Console Board",
                        KeyMap.requestMapping("open_tool_board_console", KeyMap.identifierToStrokes("c+" + KeyEvent.VK_3 + ";c+" + KeyEvent.VK_NUMPAD3)).setGroupName("Windows"),
                        () -> {
                            GuardianWindow.consoleBoard.open();
                            GuardianWindow.consoleBoard.scrollToBottom();
                        }
                ).setIconKey("console")
        );
        actions.put("OPEN_SEARCH_RESULTS",
                new ProgramAction(
                        "Show Search Results", "Open Search Results Board",
                        KeyMap.requestMapping("open_tool_board_search_results", KeyMap.identifierToStrokes("c+" + KeyEvent.VK_4 + ";c+" + KeyEvent.VK_NUMPAD4)).setGroupName("Windows"),
                        () -> GuardianWindow.findBoard.open()
                ).setIconKey("search")
        );
        actions.put("OPEN_PROCESSES",
                new ProgramAction(
                        "Show Processes", "Open Processes Board",
                        KeyMap.requestMapping("open_tool_board_processes", KeyMap.identifierToStrokes("c+" + KeyEvent.VK_5 + ";c+" + KeyEvent.VK_NUMPAD5)).setGroupName("Windows"),
                        () -> GuardianWindow.processBoard.open()
                ).setIconKey("process")
        );
        actions.put("RENAME_EXPLORER_FILE",
                new ProgramAction(
                        "Rename file", "Rename the file currently selected in the explorer",
                        KeyMap.requestMapping("rename_explorer_file", KeyMap.identifierToStrokes("" + KeyEvent.VK_F2)).setGroupName("Explorer"),
                        FileManager::renameSelected
                ).setUsableFunction(p -> GuardianWindow.projectExplorer.hasFocus()).setIconKey("rename")
        );
        actions.put("DELETE_EXPLORER_FILE",
                new ProgramAction(
                        "Delete file", "Delete the file currently selected in the explorer",
                        KeyMap.requestMapping("delete_explorer_file", KeyMap.identifierToStrokes("" + KeyEvent.VK_DELETE)).setGroupName("Explorer"),
                        FileManager::deleteSelected
                ).setUsableFunction(p -> GuardianWindow.projectExplorer.hasFocus())
        );
        actions.put("ABOUT",
                new ProgramAction(
                        "About", "About this program",
                        null,
                        () -> AboutPane.INSTANCE.setVisible(true)
                ).setIconKey("help")
        );
        actions.put("EXIT",
                new ProgramAction(
                        "Exit", "Close " + Guardian.core.getProgramName(),
                        null,
                        GuardianWindow::close
                )
        );
    }

    private static long ctrlWasDown = -1L;
    private static long altWasDown = -1L;
    private static boolean altGraphCaught = false;

    public static void setup() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((e) -> {
            if(KeyStrokeDialog.isVisible()) return false;
            if(e.getID() == KeyEvent.KEY_PRESSED) {
                if(!altGraphCaught) {
                    if(e.getKeyCode() == KeyEvent.VK_CONTROL) {
                        ctrlWasDown = e.getWhen();
                    } else if(e.getKeyCode() == KeyEvent.VK_ALT) {
                        altWasDown = e.getWhen();
                    }
                    if(e.isControlDown() && e.isAltDown() && altWasDown == e.getWhen() && altWasDown - ctrlWasDown >= 0 && altWasDown - ctrlWasDown <= 1 && ctrlWasDown > 0) {
                        altGraphCaught = true;
                    }
                }
            } else if(altGraphCaught && e.getID() == KeyEvent.KEY_RELEASED) {
                if(e.getKeyCode() == KeyEvent.VK_CONTROL || e.getKeyCode() == KeyEvent.VK_ALT) {
                    altGraphCaught = false;
                }
            }
            if(!altGraphCaught) {
                for(ProgramAction action : actions.values()) {
                    if(action.getShortcut() != null && action.isUsable()) {
                        if(action.getShortcut().wasPerformedExact(e)) {
                            if(e.getID() == KeyEvent.KEY_PRESSED) {
                                action.perform();
                            }
                            e.consume();
                            return true;
                        }
                    }
                }
            }
            return false;
        });
    }

    public static void putAction(String key, ProgramAction action) {
        actions.put(key, action);
    }

    public static Map<String, ProgramAction> getAllActions() {
        return actions;
    }

    public static ProgramAction getAction(String key) {
        return actions.get(key);
    }

    public static void performActionForSpecial(UserKeyBind.Special special) {
        for(ProgramAction action : actions.values()) {
            if(action.getShortcut() == null) continue;
            boolean performed = false;
            for(UserMapping mapping : action.getShortcut().getAllMappings()) {
                if(mapping instanceof SpecialMapping && ((SpecialMapping) mapping).getSpecial() == special) {
                    performed = true;
                    break;
                }
            }
            if(performed) action.perform();
        }
    }

    static {
        ConsoleBoard.registerCommandHandler("run", new ConsoleBoard.CommandHandler() {
            @Override
            public String getDescription() {
                return "Performs a program action with the given key";
            }

            @Override
            public void printHelp() {
                Debug.log();
                Debug.log("RUN: Performs a program action with the given key");
                Debug.log("Valid keys: " + actions.keySet());
            }

            @Override
            public void handle(String[] args, String rawArgs) {
                if(args.length <= 1) {
                    printHelp();
                } else {
                    String key = args[1].toUpperCase(Locale.ENGLISH);
                    ProgramAction action = actions.get(key);
                    if(action == null) {
                        Debug.log("Error: Unknown action '" + key + "'");
                    } else {
                        Debug.log("Performing action '" + key + "'...");
                        action.perform();
                    }
                }
            }
        });
    }
}
