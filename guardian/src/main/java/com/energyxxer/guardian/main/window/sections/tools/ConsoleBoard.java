package com.energyxxer.guardian.main.window.sections.tools;

import com.energyxxer.guardian.global.Preferences;
import com.energyxxer.guardian.global.ProcessManager;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.editor.behavior.AdvancedEditor;
import com.energyxxer.guardian.ui.modules.FileModuleToken;
import com.energyxxer.guardian.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.guardian.ui.styledcomponents.StyledLabel;
import com.energyxxer.guardian.ui.styledcomponents.StyledTextField;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.logger.Debug;
import com.energyxxer.util.out.ConsoleOutputStream;
import com.energyxxer.util.processes.AbstractProcess;
import com.energyxxer.xswing.ScalableDimension;

import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by User on 12/15/2016.
 */
public class ConsoleBoard extends ToolBoard {

    private static final int CONSOLE_HEIGHT = 300;
    private final OverlayScrollPane consoleScrollPane;
    private final StyledTextField inputField;

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private final ArrayList<String> commandHistory = new ArrayList<>();
    private int selectedCommand = -1;
    private String writingCommand = null;

    private static HashMap<String, CommandHandler> commandHandlers = new HashMap<>();

    private transient Process runningProcess = null;

    public ConsoleBoard(ToolBoardMaster parent) {
        super(parent);
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new ScalableDimension(0, CONSOLE_HEIGHT));

        JTextPane console = new JTextPane();
        console.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        tlm.addThemeChangeListener(t -> {
            console.setBackground(t.getColor(Color.WHITE, "Console.background"));
            console.setSelectionColor(t.getColor(new Color(50, 100, 175), "Console.selection.background","General.textfield.selection.background"));
            console.setSelectedTextColor(t.getColor(Color.BLACK, "Console.selection.foreground","General.textfield.selection.foreground","Console.foreground","General.foreground"));
            console.setFont(t.getFont(AdvancedEditor.DEFAULT_FONT, "Console", "Editor").deriveFont((float)Preferences.getModifiedEditorFontSize()));
            console.setForeground(t.getColor(Color.BLACK, "Console.foreground"));

            if(console.getStyle("warning") != null) console.removeStyle("warning");
            if(console.getStyle("error") != null) console.removeStyle("error");

            Style warningStyle = console.addStyle("warning", null);
            StyleConstants.setForeground(warningStyle, t.getColor(new Color(255, 140, 0), "Console.warning"));

            Style errorStyle = console.addStyle("error", null);
            StyleConstants.setForeground(errorStyle, t.getColor(new Color(200,50,50), "Console.error"));

            Style debugStyle = console.addStyle("debug", null);
            StyleConstants.setForeground(debugStyle, new Color(104,151,187));
        });
        /*clear.addActionListener(e -> {
            try {
                console.getDocument().remove(0,console.getDocument().getLength());
            } catch(BadLocationException x) {}
        });*/
        console.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                AttributeSet hyperlink = console.getStyledDocument().getCharacterElement(console.viewToModel(e.getPoint())).getAttributes();
                if(hyperlink.containsAttribute("IS_HYPERLINK",true)) {
                    String path = (String) hyperlink.getAttribute("PATH");
                    int location = Integer.parseInt((String) hyperlink.getAttribute("LOCATION"));
                    int length = Integer.parseInt((String) hyperlink.getAttribute("LENGTH"));

                    GuardianWindow.tabManager.openTab(new FileModuleToken(new File(path)), location, length);
                }
            }
        });
        console.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                AttributeSet hyperlink = console.getStyledDocument().getCharacterElement(console.viewToModel(e.getPoint())).getAttributes();

                console.setCursor(Cursor.getPredefinedCursor((hyperlink.containsAttribute("IS_HYPERLINK",true)) ? Cursor.HAND_CURSOR : Cursor.TEXT_CURSOR));
            }
        });
        console.setEditable(false);
        console.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        //console.setEditorKit(JEditorPane.createEditorKitForContentType("text/html"));

        //tlm.addThemeChangeListener(t -> textConsoleOut.update());

        Debug.addStream(new ConsoleOutputStream(console));
        /*Console.addInfoStream(new ConsoleOutputStream(console));
        Console.addWarnStream(new ConsoleOutputStream(console,"warning"));
        Console.addErrStream(new ConsoleOutputStream(console,"error"));
        Console.addDebugStream(new ConsoleOutputStream(console,"debug"));*/

        //consoleOut = new PrintStream(textConsoleOut);
        //System.setOut(new PrintStream(new MultiOutputStream(consoleOut, System.out)));
        //System.setErr(new PrintStream(new MultiOutputStream(consoleOut, System.err)));

        JPanel consoleWrapper = new JPanel(new BorderLayout());
        consoleWrapper.add(console);
        consoleScrollPane = new OverlayScrollPane(tlm, consoleWrapper);


        this.add(consoleScrollPane, BorderLayout.CENTER);
        JPanel fieldPane = new JPanel(new BorderLayout());
        this.add(fieldPane, BorderLayout.SOUTH);
        StyledLabel fieldLabel;
        fieldPane.add(fieldLabel = new StyledLabel("",tlm), BorderLayout.WEST);
        fieldLabel.setIconName("console_input");
        fieldPane.add(inputField = new StyledTextField("", "Console", tlm));

        tlm.addThemeChangeListener(t -> {
            inputField.setPreferredSize(new ScalableDimension(1, 24));
            inputField.setFont(t.getFont(AdvancedEditor.DEFAULT_FONT, "Console", "Editor").deriveFont((float)Preferences.getModifiedEditorFontSize()));
            fieldPane.setBackground(inputField.getBackground());
            consoleScrollPane.setBackground(console.getBackground());
            consoleScrollPane.setBorder(BorderFactory.createMatteBorder(Math.max(t.getInteger("Console.header.border.thickness"),0), 0, 0, 0, t.getColor(new Color(200, 200, 200), "Console.header.border.color")));
        });

        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getModifiers() != 0) return;
                if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    String command = inputField.getText();
                    inputField.setText("");
                    submitCommand(command, true);
                } else if(e.getKeyCode() == KeyEvent.VK_UP) {
                    getPreviousCommand();
                } else if(e.getKeyCode() == KeyEvent.VK_DOWN) {
                    getNextCommand();
                }
            }
        });

        registerCommandHandler("clear", new CommandHandler() {
            @Override
            public String getDescription() {
                return "Clears the console";
            }

            @Override
            public void printHelp() {
                Debug.log();
                Debug.log("CLEAR: Clears the console");
            }

            @Override
            public void handle(String[] args, String rawArgs) {
                console.setText("");
            }
        });
        registerCommandHandler("help", new CommandHandler() {
            @Override
            public String getDescription() {
                return "Shows this message";
            }

            @Override
            public void printHelp() {
                Debug.log();
                Debug.log("HELP: Shows a list of all commands");
            }

            @Override
            public void handle(String[] args, String rawArgs) {
                if(args.length <= 1) {
                    Debug.log();
                    Debug.log("Available Commands:");

                    for(Map.Entry<String, CommandHandler> entry : commandHandlers.entrySet()) {
                        Debug.log("  > " + entry.getKey() + ": " + entry.getValue().getDescription());
                    }
                } else {
                    if(commandHandlers.containsKey(args[1])) {
                        commandHandlers.get(args[1]).printHelp();
                    } else {
                        Debug.log("Unknown command '" + args[1] + "'");
                    }
                }
            }
        });
        ConsoleBoard.registerCommandHandler("exec", new ConsoleBoard.CommandHandler() {
            @Override
            public String getDescription() {
                return "Executes a program";
            }

            @Override
            public void printHelp() {
                Debug.log();
                Debug.log("EXEC: Runs a program");
            }

            private ArrayList<String> splitCommandIntoArgs(String command) {
                ArrayList<String> args = new ArrayList<>();
                StringBuilder sb = new StringBuilder();
                boolean escaped = false;
                boolean inString = false;
                for(char c : command.toCharArray()) {
                    if(Character.isWhitespace(c) && !inString) {
                        args.add(sb.toString());
                        sb.setLength(0);
                        escaped = false;
                    } else if(c == '\"' && !escaped) {
                        inString = !inString;
                        sb.append(c);
                    } else if(c == '\\') {
                        escaped = !escaped;
                        sb.append(c);
                    } else {
                        escaped = false;
                        sb.append(c);
                    }
                }
                if(sb.length() > 0) {
                    args.add(sb.toString());
                }
                return args;
            }

            @Override
            public void handle(String[] args, String rawArgs) {
                String command = String.join(" ", args).substring("exec".length()).trim();
                ProcessBuilder pb = new ProcessBuilder(splitCommandIntoArgs(command)).redirectErrorStream(true);

                GuardianWindow.consoleBoard.lock();

                try {
                    ProcessManager.queueProcess(new AbstractProcess("Console Process") {

                        {
                            initializeThread(this::startProcess);
                        }

                        private void startProcess() {
                            try {
                                Debug.log("Starting process \"" + command + "\"\n");
                                Process process = pb.start();

                                GuardianWindow.consoleBoard.attachProcess(process);
                                GuardianWindow.consoleBoard.unlock();

                                BufferedReader stdInput = new BufferedReader(new
                                        InputStreamReader(process.getInputStream()));

                                String s = null;
                                while ((s = stdInput.readLine()) != null) {
                                    Debug.log(s, Debug.MessageType.PLAIN);
                                }

                                Debug.log("Process finished with exit code " + process.exitValue(), Debug.MessageType.PLAIN);
                                GuardianWindow.consoleBoard.detachProcess();
                            } catch(Exception x) {
                                GuardianWindow.showException(x);
                            }

                            finalizeProcess(true);
                        }

                    });
                } catch (Exception x) {
                    GuardianWindow.showException(x);
                }
            }
        });
    }

    private transient boolean locked = false;

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }

    public boolean waitForUnlock() {
        return locked;
    }

    public void submitCommand(String command, boolean addToHistory) {
        if(command == null) return;
        if(runningProcess != null && runningProcess.isAlive()) {
            addToHistory(command);
            try {
                runningProcess.getOutputStream().write((command + System.getProperty("line.separator")).getBytes());
                runningProcess.getOutputStream().flush();
                scrollToBottom();
            } catch (IOException ex) {
                GuardianWindow.showException(ex);
            }
        } else {
            runningProcess = null;
            if(!command.isEmpty()) {
                if(addToHistory) addToHistory(command);
                runCommand(command);
            }
        }
    }

    public void batchSubmitCommand(Iterable<String> commands) {
        if(commands == null) return;
        for(String command : commands) {
            submitCommand(command, false);
            while(waitForUnlock()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException x) {
                    GuardianWindow.showException(x);
                    x.printStackTrace();
                }
            }
        }

        if(runningProcess != null) {
            open();
            scrollToBottom();
            GuardianWindow.statusBar.setStatus("Waiting for user input...");
        }

        while(runningProcess != null) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException x) {
                GuardianWindow.showException(x);
                x.printStackTrace();
            }
        }
    }

    private void addToHistory(String command) {
        if(commandHistory.isEmpty() || !command.equals(commandHistory.get(0))) {
            commandHistory.add(0, command);
        }
        selectedCommand = -1;
        writingCommand = "";
    }

    private void getPreviousCommand() {
        if(selectedCommand == -1) {
            writingCommand = inputField.getText();
        }
        if(selectedCommand+1 < commandHistory.size()) {
            selectedCommand++;
            inputField.setText(commandHistory.get(selectedCommand));
        }
    }

    private void getNextCommand() {
        if(selectedCommand-1 >= -1 && selectedCommand-1 < commandHistory.size()) {
            selectedCommand--;
            if(selectedCommand > -1) {
                inputField.setText(commandHistory.get(selectedCommand));
            } else {
                inputField.setText(writingCommand);
            }
        }
    }

    @Override
    public void open() {
        super.open();
        inputField.requestFocus();
        consoleScrollPane.getHorizontalScrollBar().setValue(0);
        consoleScrollPane.getVerticalScrollBar().setValue(consoleScrollPane.getViewport().getViewSize().height);
    }

    @Override
    public String getName() {
        return "Console";
    }

    @Override
    public String getIconName() {
        return "console";
    }

    private void runCommand(String command) {
        String[] args = command.split(" ", -1);
        if(commandHandlers.containsKey(args[0])) {
            commandHandlers.get(args[0]).handle(args, command);
        } else {
            Debug.log("Unknown command '" + args[0] + "'");
        }
    }

    public static void registerCommandHandler(String commandName, CommandHandler handler) {
        commandHandlers.put(commandName, handler);
    }

    public void scrollToBottom() {
        consoleScrollPane.scrollRectToVisible(new Rectangle(0, consoleScrollPane.getViewport().getHeight()-1, 1, 1));
    }

    public void attachProcess(Process process) {
        this.runningProcess = process;
    }

    public void detachProcess() {
        runningProcess = null;
    }

    public interface CommandHandler {
        String getDescription();
        void printHelp();
        void handle(String[] args, String rawArgs);
    }
}
