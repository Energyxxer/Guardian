package com.energyxxer.guardian.main.window;

import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.global.Resources;
import com.energyxxer.guardian.global.Status;
import com.energyxxer.guardian.global.TabManager;
import com.energyxxer.guardian.global.keystrokes.UserKeyBind;
import com.energyxxer.guardian.main.Guardian;
import com.energyxxer.guardian.main.window.actions.ActionManager;
import com.energyxxer.guardian.main.window.sections.MenuBar;
import com.energyxxer.guardian.main.window.sections.*;
import com.energyxxer.guardian.main.window.sections.tools.ConsoleBoard;
import com.energyxxer.guardian.main.window.sections.tools.NoticeBoard;
import com.energyxxer.guardian.main.window.sections.tools.ToolBoardMaster;
import com.energyxxer.guardian.main.window.sections.tools.find.FindBoard;
import com.energyxxer.guardian.main.window.sections.tools.process.ProcessBoard;
import com.energyxxer.guardian.main.window.sections.tools.todo.TodoBoard;
import com.energyxxer.guardian.ui.HintStylizer;
import com.energyxxer.guardian.ui.common.KeyFixDialog;
import com.energyxxer.guardian.ui.editor.completion.snippets.SnippetManager;
import com.energyxxer.guardian.ui.explorer.NoticeExplorerMaster;
import com.energyxxer.guardian.ui.explorer.ProjectExplorerMaster;
import com.energyxxer.guardian.ui.misc.ExceptionHint;
import com.energyxxer.guardian.ui.tablist.TabListMaster;
import com.energyxxer.guardian.ui.theme.Theme;
import com.energyxxer.guardian.ui.theme.ThemeManager;
import com.energyxxer.guardian.ui.theme.change.ThemeChangeListener;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.ImageManager;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.xswing.OverlayBorderLayout;
import com.energyxxer.xswing.ScalableDimension;
import com.energyxxer.xswing.TemporaryConfirmation;
import com.energyxxer.xswing.hints.Hint;
import com.energyxxer.xswing.hints.HintManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.energyxxer.xswing.KeyInputUtils.*;

/**
 * Literally what it sounds like.
 */
public class GuardianWindow {
	private static final Dimension defaultSize = new ScalableDimension(1200, 800);
	public static final Font defaultFont = new JLabel().getFont();

	public static JFrame jframe;

	public static ProjectExplorerMaster projectExplorer;
	public static NoticeExplorerMaster noticeExplorer;

	public static ToolBoardMaster toolBoard;

	public static TodoBoard todoBoard;
	public static NoticeBoard noticeBoard;
	public static ConsoleBoard consoleBoard;
	public static FindBoard findBoard;
	public static ProcessBoard processBoard;

	public static MenuBar menuBar;
	public static Toolbar toolbar;
	public static Sidebar sidebar;
	public static EditArea editArea;
	public static WelcomePane welcomePane;

	public static StatusBar statusBar;

	public static HintManager hintManager = new HintManager(jframe);
	public static Hint exceptionHint = hintManager.createHint(ExceptionHint::new);

	public static TabManager tabManager;
	public static TabListMaster tabList;

	public ThemeListenerManager tlm = new ThemeListenerManager();

	private static KeyFixDialog recentlyShownDialog = null;

	public static Robot robot;

    public GuardianWindow() {
		jframe = new JFrame();
		setTitle("");
		jframe.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		if(Preferences.get("meta.delete_old_jar", null) != null) {
			File oldJar = new File(Preferences.get("meta.delete_old_jar"));
			if(oldJar.delete()) {
				Preferences.remove("meta.delete_old_jar");
				Debug.log("Deleted '" + oldJar + "'");
			}
		}

		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}

		tlm.addThemeChangeListener(t -> jframe.getContentPane().setBackground(t.getColor(new Color(215, 215, 215), "Window.background")));

		jframe.setLayout(new BorderLayout());

		tabList = new TabListMaster();
		tabManager = new TabManager(tabList, c -> editArea.setContent(c));
		tabManager.setChangeWindowInfo(true);
		tabManager.setOpenTabSaveKey("open_tabs");

		welcomePane = new WelcomePane();

		JPanel mainContent = new JPanel(new OverlayBorderLayout());
		jframe.getContentPane().add(mainContent, BorderLayout.CENTER);

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher((e) -> {
			if(e.getID() == KeyEvent.KEY_PRESSED) {
				if(e.getKeyCode() == KeyEvent.VK_SHIFT && isDoublePress(e)) {
					ActionManager.performActionForSpecial(UserKeyBind.Special.DOUBLE_SHIFT);
					return true;
				} else if(e.getKeyCode() != KeyEvent.VK_SHIFT && e.isShiftDown()) {
					interruptDoublePresses();
				}
			} else if(e.getID() == KeyEvent.KEY_RELEASED) {
				if(e.getKeyCode() == KeyEvent.VK_SHIFT) {
					markRelease(e);
				}
			}
			if(jframe.getFocusOwner() == null) {
				// Missed key event
				if(recentlyShownDialog != null) {
					if(recentlyShownDialog.isVisible()) {
						// Attributing to dialog
						recentlyShownDialog.requestFocusInWindow();
						if(e.getID() == KeyEvent.KEY_TYPED) {
							recentlyShownDialog.keyTyped(e);
						} else if(e.getID() == KeyEvent.KEY_PRESSED) {
							recentlyShownDialog.keyPressed(e);
						} else if(e.getID() == KeyEvent.KEY_RELEASED) {
							recentlyShownDialog.keyReleased(e);
						}
						return true;
					}
					// Dropped
				}
			}
            return false;
        });

		toolBoard = new ToolBoardMaster();
		mainContent.add(toolBoard, BorderLayout.SOUTH);
		mainContent.add(sidebar = new Sidebar(), BorderLayout.WEST);

		todoBoard = new TodoBoard(toolBoard);
		noticeBoard = new NoticeBoard(toolBoard);
		consoleBoard = new ConsoleBoard(toolBoard);
		findBoard = new FindBoard(toolBoard);
		processBoard = new ProcessBoard(toolBoard);
		toolBoard.setLastOpenedBoard(consoleBoard);

		mainContent.add(editArea = new EditArea(), BorderLayout.CENTER);

		jframe.getContentPane().add(statusBar = new StatusBar(), BorderLayout.SOUTH);

		ActionManager.setup();
		Guardian.core.setupActions();
		jframe.getContentPane().add(toolbar = new Toolbar(), BorderLayout.NORTH);
		jframe.setJMenuBar(menuBar = new MenuBar());

		jframe.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				terminate();
			}
		});

		jframe.setSize(defaultSize);
		jframe.setPreferredSize(defaultSize);
		jframe.setExtendedState(JFrame.MAXIMIZED_BOTH);

		List<Image> icons = new ArrayList<>();
		icons.add(
				ImageManager.load("/assets/logo/logo_icon.png").getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH));
		icons.add(ImageManager.load("/assets/logo/logo.png").getScaledInstance(32, 32, java.awt.Image.SCALE_SMOOTH));
		jframe.setIconImages(icons);

		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Point center = env.getCenterPoint();
		center.x -= jframe.getWidth() / 2;
		center.y -= jframe.getHeight() / 2;
		jframe.setLocation(center);

		tlm.addThemeChangeListener(t -> {
			UIManager.put("ToolTip.background",t.getColor(Color.WHITE, "Tooltip.background"));
			UIManager.put("ToolTip.foreground",t.getColor(Color.BLACK, "Tooltip.foreground"));
			int borderThickness = Math.max(t.getInteger(1,"Tooltip.border.thickness"),0);
			UIManager.put("ToolTip.border",BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(borderThickness, borderThickness, borderThickness, borderThickness, t.getColor(Color.BLACK, "Tooltip.border.color")),BorderFactory.createEmptyBorder(3,5,3,5)));
		});
	}

	public static void setVisible(boolean b) {
		jframe.setVisible(b);
	}

	public static boolean isVisible() {
		return jframe.isVisible();
	}

	public static void setTheme(Theme t) {
		if(statusBar != null && !t.equals(ThemeManager.currentGUITheme)) {
			Status themeSetStatus = new Status("Theme set to: " + t.getName());

			setStatus(themeSetStatus);

			new Timer().schedule(new TimerTask() {
				public void run() {
					GuardianWindow.dismissStatus(themeSetStatus);
				}
			}, 5000);
		}

		ThemeChangeListener.dispatchThemeChange(t);
	}

    public static Theme getTheme() {
        return ThemeManager.currentGUITheme;
    }

	public static void setStatus(String text) {
		statusBar.setStatus(text);
	}

	public static void setStatus(Status status) {
		statusBar.setStatus(status);
	}

	public static void setTitle(String title) {
		jframe.setTitle(title + ((title.length() > 0) ? " - " : "") + Guardian.core.getProgramName() + " " + Guardian.core.getDisplayedVersion());
	}

	public static void clearTitle() {
		setTitle("");
	}

	public static void dismissStatus(Status status) {
		statusBar.dismissStatus(status);
	}

	public static void showException(Exception x) {
    	showException(x.getMessage());
	}

	public static void showException(String message) {
		GuardianWindow.statusBar.setStatus(new Status(Status.ERROR, message));
    	showError(message + "\nSee console for details");
	}

	public static synchronized void showError(String message) {
		HintStylizer.style(exceptionHint, "error");
		exceptionHint.setPreferredPos(Hint.LEFT);
		exceptionHint.setArrowVisible(false);
		((ExceptionHint) exceptionHint).setText(message);
		exceptionHint.show(new Point(jframe.getX() + jframe.getWidth() - 15, jframe.getY() + jframe.getHeight() - 53 - 15), new TemporaryConfirmation(5 + message.length() / 10));
	}

	public static synchronized void showPopupMessage(String message) {
		HintStylizer.style(exceptionHint, "info");
		exceptionHint.setPreferredPos(Hint.LEFT);
		exceptionHint.setArrowVisible(false);
		((ExceptionHint) exceptionHint).setText(message);
		exceptionHint.show(new Point(jframe.getX() + jframe.getWidth() - 15, jframe.getY() + jframe.getHeight() - 53 - 15), new TemporaryConfirmation(5 + message.length() / 10));
	}

	public static void dialogShown(KeyFixDialog dialog) {
		recentlyShownDialog = dialog;
	}

    public static void close() {
		jframe.dispatchEvent(new WindowEvent(jframe, WindowEvent.WINDOW_CLOSING));
	}

	private static File restartingJar = null;

	public static void setRestartingJar(File value) {
		restartingJar = value;
	}

	private static void terminate() {
		if(!tabManager.confirmSaved()) {
			return;
		}

		Debug.log("Terminating...");

		tabManager.saveOpenTabs();
		projectExplorer.saveExplorerTree();
		Resources.saveAll();
		SnippetManager.save();
		jframe.dispose();
		if(restartingJar != null) {
			try {
				String javaPath = System.getProperty("java.home")
						+ File.separator + "bin" + File.separator + "java";
				ProcessBuilder pb = new ProcessBuilder(javaPath, "-jar", restartingJar.getPath());
				pb.start();
				System.exit(0);
			} catch (IOException e) {
				e.printStackTrace();
			}
			restartingJar = null;
		} else {
			System.exit(0);
		}
	}
}