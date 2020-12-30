package com.energyxxer.guardian.main;

import com.energyxxer.commodore.versioning.ThreeNumberVersion;
import com.energyxxer.guardian.GuardianCore;
import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.global.Resources;
import com.energyxxer.guardian.global.temp.projects.ProjectManager;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.main.window.sections.tools.ConsoleBoard;
import com.energyxxer.guardian.ui.audio.AudioPlayer;
import com.energyxxer.guardian.ui.commodoreresources.DefinitionUpdateProcess;
import com.energyxxer.guardian.ui.common.ProgramUpdateProcess;
import com.energyxxer.guardian.ui.editor.completion.snippets.SnippetManager;
import com.energyxxer.guardian.ui.imageviewer.ImageViewer;
import com.energyxxer.guardian.ui.modules.FileModuleToken;
import com.energyxxer.guardian.util.LineReader;
import com.energyxxer.util.ImageManager;
import com.energyxxer.util.logger.Debug;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class Guardian {
	public static final String LICENSE = "MIT License\n" +
			"\n" +
			"Copyright (c) 2020 Daniel Cepeda (Energyxxer)\n" +
			"\n" +
			"Permission is hereby granted, free of charge, to any person obtaining a copy\n" +
			"of this software and associated documentation files (the \"Software\"), to deal\n" +
			"in the Software without restriction, including without limitation the rights\n" +
			"to use, copy, modify, merge, publish, distribute, sublicense, and/or sell\n" +
			"copies of the Software, and to permit persons to whom the Software is\n" +
			"furnished to do so, subject to the following conditions:\n" +
			"\n" +
			"The above copyright notice and this permission notice shall be included in all\n" +
			"copies or substantial portions of the Software.\n" +
			"\n" +
			"THE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR\n" +
			"IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,\n" +
			"FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE\n" +
			"AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER\n" +
			"LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,\n" +
			"OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE\n" +
			"SOFTWARE.\n";

	public static ThreeNumberVersion VERSION = new ThreeNumberVersion(1, 0, 0);
	public static Guardian guardian;
	public static GuardianCore core;
	public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	public static File RUNNING_PATH;

	static {
		try {
			RUNNING_PATH = new File(URLDecoder.decode(Guardian.class.getProtectionDomain().getCodeSource().getLocation().getPath(), StandardCharsets.UTF_8.name()));
		} catch(Exception x) {
			x.printStackTrace();
			RUNNING_PATH = null;
		}
	}

	public static GuardianWindow window;

	private Guardian() {
		window = new GuardianWindow();
	}

	public static void main(String[] args) {
		Debug.addStream(System.out);

		loadCore();

		try {
			Debug.addStream(new FileOutputStream(core.getLogFile()));
		} catch(FileNotFoundException x) {
			Debug.log("Unable to open log file '" + core.getLogFile() + "', will not log to it until restarted");
		}
		Debug.log("Running on Java " + System.getProperty("java.version"));

		SwingUtilities.invokeLater(Guardian::showSplash);
		loadBindings();
		Resources.load();
		SwingUtilities.invokeLater(Guardian::start);
	}

	private static void loadCore() {
		try {
			ArrayList<String> lr = LineReader.read("/guardian_core.txt");
			for(String className : lr) { //just need one
				Class cls = ClassLoader.getSystemClassLoader().loadClass(className);
				//noinspection unchecked
				core = (GuardianCore) cls.getConstructor().newInstance();
				break; //just one line allowed
			}
			if(core == null) {
				Debug.log("guardian_core.txt was malformed or not found. Proceeding with the default core", Debug.MessageType.WARN);
				core = new GuardianCore();
			}
		} catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static void loadBindings() {
		FileModuleToken.addDisplayModuleProvider(f -> {
			if(f.getName().endsWith(".png")) {
				return new ImageViewer(f);
			}
			return null;
		});
		FileModuleToken.addDisplayModuleProvider(f -> {
			String name = f.getName();
			if(name.endsWith(".ogg") || name.endsWith(".wav") || name.endsWith(".mp3") || name.endsWith(".aiff") || name.endsWith(".aif") || name.endsWith(".aifc") || name.endsWith(".au") || name.endsWith(".snd")) {
				return new AudioPlayer(f);
			}
			return null;
		});
		try {
			ArrayList<String> lr = LineReader.read("/guardian_bindings.txt");
			for(String className : lr) {
				Class cls = ClassLoader.getSystemClassLoader().loadClass(className);
				//noinspection unchecked
				cls.getMethod("setup").invoke(null);
			}
		} catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static JFrame splash = null;

	private static void showSplash() {
		splash = new JFrame();
		splash.setSize(new Dimension(700, 410));
		Point center = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		center.x -= 700/2;
		center.y -= 410/2;
		splash.setLocation(center);
		splash.setUndecorated(true);
		splash.setVisible(true);
		Point versionCoords = core.getSplashVersionCoords();
		splash.setContentPane(new JComponent() {
			@Override
			protected void paintComponent(Graphics g) {
				g.drawImage(ImageManager.load("/assets/logo/splash.png"), 0,0,this.getWidth(),this.getHeight(), null);

				Graphics2D g2d = (Graphics2D) g;
				g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setColor(new Color(187, 187, 187));
				g.setFont(g.getFont().deriveFont(21f));
				g.drawString(core.getDisplayedVersion(), versionCoords.x, versionCoords.y);
				g.dispose();
			}
		});
		splash.revalidate();
		splash.setIconImage(ImageManager.load("/assets/logo/logo.png").getScaledInstance(32, 32, Image.SCALE_SMOOTH));
	}

	private static void start() {

		guardian = new Guardian();
		System.setErr(new PrintStream(new OutputStream() {
			StringBuilder sb = new StringBuilder();

			@Override
			public void flush() throws IOException {
				String message = sb.toString();

				if(message.startsWith("Exception in thread ")) {
					Debug.log("", Debug.MessageType.PLAIN);
					int start = message.indexOf(":");
					if(start == -1) start = 0;
					else start += 2;
					GuardianWindow.showException(message.substring(start));
				}
				//defaultErrStream.println(sb.toString());
				Debug.log(message, Debug.MessageType.PLAIN);

				sb.setLength(0);
			}

			@Override
			public void write(int b) throws IOException {
				if(b == '\n') {
					flush();
				} else {
					sb.append((char) b);
				}
			}
		}));

		GuardianWindow.setVisible(true);

		splash.setVisible(false);
		splash.dispose();
		splash = null;

		if(Preferences.get("workspace_dir", null) == null) {
			WorkspaceDialog.prompt();
		}

		ProjectManager.loadWorkspace();

		GuardianWindow.welcomePane.tipScreen.start(1000);
		GuardianWindow.tabManager.openSavedTabs();
		GuardianWindow.projectExplorer.openExplorerTree();
		SnippetManager.load();

		Guardian.core.startupComplete();

		if(ProgramUpdateProcess.CHECK_FOR_PROGRAM_UPDATES_STARTUP.get()) ProgramUpdateProcess.tryUpdate();
		if(DefinitionUpdateProcess.CHECK_FOR_DEF_UPDATES_STARTUP.get()) DefinitionUpdateProcess.tryUpdate();

		ConsoleBoard.registerCommandHandler("license", new ConsoleBoard.CommandHandler() {
			@Override
			public String getDescription() {
				return "Displays the " + core.getProgramName() + " license";
			}

			@Override
			public void printHelp() {
				Debug.log();
				Debug.log("LICENSE: Displays the " + core.getProgramName() + " license");
			}

			@Override
			public void handle(String[] args) {
				Debug.log("\n"+core.getLicense());
			}
		});
		ConsoleBoard.registerCommandHandler("exception", new ConsoleBoard.CommandHandler() {
			@Override
			public String getDescription() {
				return "Throws a runtime exception";
			}

			@Override
			public void printHelp() {
				Debug.log();
				Debug.log("EXCEPTION: Throws a runtime exception");
			}

			@Override
			public void handle(String[] args) {
				throw new RuntimeException("Exception triggered via the console");
			}
		});

		Debug.log("java path: " + System.getProperty("java.home")
				+ File.separator + "bin" + File.separator + "java");
	}

}
