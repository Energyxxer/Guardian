package com.energyxxer.guardian.ui.common;

import com.energyxxer.guardian.files.FileType;
import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.langinterface.ProjectType;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.dialogs.file_dialogs.ProjectDialog;
import com.energyxxer.guardian.ui.dialogs.file_dialogs.ProjectFromTemplateDialog;
import com.energyxxer.guardian.ui.modules.FileModuleToken;
import com.energyxxer.guardian.ui.styledcomponents.StyledMenu;
import com.energyxxer.guardian.ui.styledcomponents.StyledMenuItem;

import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.function.Predicate;

import static com.energyxxer.guardian.main.window.sections.MenuBar.createItemForAction;

/**
 * Provides managers that create menu components for file and project management.
 */
public class MenuItems {
	private static StyledMenu CHANGE_WORKSPACE_MENU;

	public static StyledMenu newMenu(String title, Predicate<FileType> canCreate, String newPath, boolean permanent) {
		StyledMenu newMenu = new StyledMenu(title);

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
		anyMenuItem = false;

		// --------------------------------------------------


		int prevGroup = 0;
		for(FileType type : FileType.values()) {
			if(canCreate.test(type)) {
				if(type.group != prevGroup) {
					if(anyMenuItem) newMenu.addSeparator();
					prevGroup = type.group;
				}

				if(permanent) {
					newMenu.add(createNewFileItem(type));
				} else {
					newMenu.add(type.createMenuItem(newPath));
				}
				anyMenuItem = true;
			}
		}

		// --------------------------------------------------

		if(anyMenuItem) newMenu.addSeparator();
		anyMenuItem = false;

		newMenu.add(createNewProjectFromTemplateMenu(permanent));

		return newMenu;
	}

	private static StyledMenuItem createNewFileItem(FileType type) {
		StyledMenuItem item = new StyledMenuItem(type.name, type.icon);
		item.addActionListener(e -> {
			File activeFile = Commons.getActiveFile();
			if(activeFile != null) {
				if(activeFile.isFile()) activeFile = activeFile.getParentFile();
				type.create(activeFile.getPath());
			}
		});
		return item;
	}

	private static StyledMenu createNewProjectFromTemplateMenu(boolean updateOnOpen) {
		StyledMenu newMenu = new StyledMenu("Project from Template");


		StyledMenuItem defaultItem = new StyledMenuItem("Select Template");
		defaultItem.addActionListener(e -> {
			ProjectFromTemplateDialog.create(null);
		});

		newMenu.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				defaultItem.doClick();
			}
		});

		if(updateOnOpen) {
			newMenu.addMenuListener(new MenuListener() {

				private long selectedWhen;

				@Override
				public void menuSelected(MenuEvent e) {
					if(selectedWhen < System.currentTimeMillis() - 50) {
						newMenu.removeAll();
						newMenu.add(defaultItem);
						newMenu.addSeparator();

						fillProjectTemplateMenu(newMenu);
					}
					selectedWhen = System.currentTimeMillis();
				}

				@Override
				public void menuDeselected(MenuEvent e) {
				}

				@Override
				public void menuCanceled(MenuEvent e) {
				}
			});
		} else {
			newMenu.add(defaultItem);
			newMenu.addSeparator();
			fillProjectTemplateMenu(newMenu);
		}

		return newMenu;
	}

	private static void fillProjectTemplateMenu(StyledMenu newMenu) {
		List<File> templateRoots = Guardian.core.getProjectTemplateRoots();
		for(File templateRoot : templateRoots) {
			if(templateRoot.isDirectory()) {
				StyledMenuItem templateItem = new StyledMenuItem(templateRoot.getName(), "package");
				templateItem.addActionListener(e -> {
					ProjectFromTemplateDialog.create(templateRoot);
				});
				newMenu.add(templateItem);
			}
		}
	}

	public enum FileMenuItem {
		COPY, PASTE, DELETE, RENAME, MOVE
	}

	public static StyledMenuItem fileItem(FileMenuItem type) {
		StyledMenuItem item = null;
		switch (type) {
		case COPY:
			item = new StyledMenuItem("Copy");
			break;
		case DELETE:
			item = new StyledMenuItem(FileModuleToken.DELETE_MOVES_TO_TRASH.get() ? "Move to Trash" : "Delete");
			/*item.setEnabled(false);
			item.setEnabled(ExplorerMaster.selectedLabels.size() > 0);
			item.addActionListener(e -> {
				ArrayList<File> files = new ArrayList<>();
				String fileType = null;
				for(int i = 0; i < ExplorerMaster.selectedLabels.size(); i++) {
					File file = new File(ExplorerMaster.selectedLabels.get(i).parent.path);
					if(file.isFile() && fileType == null) {
						fileType = "file";
					} else if(file.isDirectory() && fileType == null) {
						fileType = "folder";
					} else if(file.isDirectory() && "file".equals(fileType)) {
						fileType = "item";
					} else if(file.isFile() && "folder".equals(fileType)) {
						fileType = "item";
					}
					files.add(file);
				}

				String subject = ((ExplorerMaster.selectedLabels.size() == 1) ? "this" : "these") + " " + ((ExplorerMaster.selectedLabels.size() == 1) ? "" : "" + ExplorerMaster.selectedLabels.size() + " ") + fileType + ((ExplorerMaster.selectedLabels.size() == 1) ? "" : "s");

				int confirmation = JOptionPane.showConfirmDialog(null,
						"        Are you sure you want to delete " + subject + "?        ",
						"Delete " + fileType, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (confirmation == JOptionPane.YES_OPTION) {
					for(File file : files) FileCommons.deleteFolder(file);
					GuardianWindow.projectExplorer.refresh();
				}
			});*/
			break;
		case MOVE:
			item = new StyledMenuItem("Move");
			item.setEnabled(GuardianWindow.projectExplorer.getSelectedTokens().size() > 0);
			break;
		case PASTE:
			item = new StyledMenuItem("Paste");
			break;
		case RENAME:
			item = new StyledMenuItem("Rename", "rename");
			item.setEnabled(GuardianWindow.projectExplorer.getSelectedTokens().size() == 1);
			break;
		default:
			break;
		}
		return item;
	}

	public static StyledMenu refactorMenu(String title) {
		StyledMenu newMenu = new StyledMenu(title);

		newMenu.add(fileItem(FileMenuItem.RENAME));
		newMenu.add(fileItem(FileMenuItem.MOVE));

		return newMenu;

	}

	public static StyledMenu changeWorkspaceMenu() {
		CHANGE_WORKSPACE_MENU = new StyledMenu("Change Workspace", "folder");
		return regenerateChangeWorkspaceMenu();
	}

	public static StyledMenu regenerateChangeWorkspaceMenu() {
		if(CHANGE_WORKSPACE_MENU == null) return null;

		CHANGE_WORKSPACE_MENU.removeAll();

		CHANGE_WORKSPACE_MENU.add(createItemForAction("CHANGE_WORKSPACE"));
		CHANGE_WORKSPACE_MENU.addSeparator();

		String[] workspaceHistoryReversed = Preferences.WORKSPACE_HISTORY.toArray(new String[0]);

		for(int i = workspaceHistoryReversed.length-1; i >= 0; i--) {
			String path = workspaceHistoryReversed[i];
			StyledMenuItem item = new StyledMenuItem(path);
			item.addActionListener(a -> {
				File dir = new File(path);
				if(dir.exists() && dir.isDirectory()) {
					Preferences.setWorkspace(dir);
				} else {
					GuardianWindow.showError("Folder '" + path + "' no longer exists.");
					Preferences.WORKSPACE_HISTORY.remove(path);
					regenerateChangeWorkspaceMenu();
				}
			});
			if(i == workspaceHistoryReversed.length-1) {
				item.setIconName("triangle_right");
			}
			CHANGE_WORKSPACE_MENU.add(item);
		}

		if(workspaceHistoryReversed.length > 1) {
			CHANGE_WORKSPACE_MENU.addSeparator();
			CHANGE_WORKSPACE_MENU.add(createItemForAction("CLEAR_WORKSPACE_HISTORY"));
		}

		return CHANGE_WORKSPACE_MENU;
	}
}
