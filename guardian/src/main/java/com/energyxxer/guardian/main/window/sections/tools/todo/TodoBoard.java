package com.energyxxer.guardian.main.window.sections.tools.todo;

import com.energyxxer.enxlex.lexical_analysis.summary.Todo;
import com.energyxxer.enxlex.lexical_analysis.token.TokenSection;
import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.global.temp.projects.Project;
import com.energyxxer.guardian.global.temp.projects.ProjectManager;
import com.energyxxer.guardian.main.window.sections.quick_find.StyledExplorerMaster;
import com.energyxxer.guardian.main.window.sections.tools.ToolBoard;
import com.energyxxer.guardian.main.window.sections.tools.ToolBoardMaster;
import com.energyxxer.guardian.main.window.sections.tools.find.FileOccurrence;
import com.energyxxer.guardian.main.window.sections.tools.find.FindExplorerFilter;
import com.energyxxer.guardian.main.window.sections.tools.find.FindResultExplorerItem;
import com.energyxxer.guardian.main.window.sections.tools.find.FindResults;
import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.ToolbarButton;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.explorer.base.StandardExplorerItem;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.guardian.ui.styledcomponents.ButtonHintHandler;
import com.energyxxer.guardian.ui.styledcomponents.Padding;
import com.energyxxer.guardian.ui.styledcomponents.StyledPopupMenu;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.util.StringBounds;
import com.energyxxer.xswing.ScalableDimension;
import com.energyxxer.xswing.hints.Hint;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class TodoBoard extends ToolBoard {

    private static class TodoExplorer extends StyledExplorerMaster implements FindExplorerFilter {
        @Override
        public boolean groupByProject() {
            return true;
        }

        @Override
        public boolean groupBySubProject() {
            return false;
        }

        @Override
        public boolean groupByPath() {
            return false;
        }

        @Override
        public boolean groupByFile() {
            return false;
        }

        @Override
        public boolean highlightResult() {
            return false;
        }
    }

    private ThemeListenerManager tlm = new ThemeListenerManager();

    private OverlayScrollPane scrollPane = new OverlayScrollPane(tlm);
    private StyledExplorerMaster explorer = new TodoExplorer();

    public TodoBoard(ToolBoardMaster parent) {
        super(parent);
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new ScalableDimension(10, 200));
        scrollPane.setViewportView(explorer);
        this.add(scrollPane);
        JPanel filterPanel = new JPanel(new BorderLayout());
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        filterPanel.add(wrapper, BorderLayout.NORTH);
        wrapper.add(new Padding(3), BorderLayout.NORTH);
        wrapper.add(new Padding(4), BorderLayout.WEST);
        wrapper.add(new Padding(4), BorderLayout.EAST);
        JPanel verticalPanel = new JPanel(new GridLayout(0, 1, 6, 6));
        verticalPanel.setOpaque(false);
        wrapper.add(verticalPanel, BorderLayout.CENTER);
        this.add(filterPanel, BorderLayout.WEST);

        ToolbarButton refreshButton = new ToolbarButton("reload", tlm);
        refreshButton.addMouseMotionListener(new ButtonHintHandler("Refresh", refreshButton, Hint.RIGHT));
        refreshButton.addActionListener(e -> refresh());
        verticalPanel.add(refreshButton);

        tlm.addThemeChangeListener(t -> {
            filterPanel.setBackground(t.getColor(Color.WHITE, "ToolBoard.header.background"));
        });
    }

    public void refresh() {
        explorer.clear();
        for(Project project : ProjectManager.getLoadedProjects()) {
            if(project.getSummary() != null) {
                Collection<Todo> todos = project.getSummary().getTodos();

                if(todos == null || todos.isEmpty()) return;

                FindResults.ProjectResult projectResult = new FindResults.ProjectResult(project.getRootDirectory());

                for(Todo todo : todos) {
                    StringBounds bounds = todo.getToken().getStringBounds();
                    TokenSection section = null;
                    for(Map.Entry<TokenSection, String> entry : todo.getToken().getSubSections().entrySet()) {
                        if(entry.getValue().equals("comment.todo")) {
                            section = entry.getKey();
                            break;
                        }
                    }
                    File file = todo.getToken().getSource().getExactFile();
                    if(file != null) {
                        FileOccurrence occurrence = new FileOccurrence(
                                file,
                                bounds.start.index + section.start,
                                section.length,
                                bounds.start.line,
                                todo.getText(),
                                0);
                        projectResult.insertResult(occurrence);
                    }
                }
                FindResultExplorerItem projectItem = new FindResultExplorerItem(projectResult, explorer, k -> true);
                projectItem.setDetailed(true);
                explorer.addElement(projectItem);
            } else {
                String projectName = project.getName();
                StandardExplorerItem placeholderItem = new StandardExplorerItem(new ModuleToken() {
                    private Collection<ModuleToken> hintTokens = Collections.singletonList(new ModuleToken() {
                        @Override
                        public String getTitle(TokenContext context) {
                            return "Open one of the project files, wait for it to index and press Refresh on the TODO board";
                        }

                        @Override
                        public Image getIcon() {
                            return Commons.getIcon("info");
                        }

                        @Override
                        public String getHint() {
                            return null;
                        }

                        @Override
                        public Collection<? extends ModuleToken> getSubTokens() {
                            return null;
                        }

                        @Override
                        public boolean isExpandable() {
                            return false;
                        }

                        @Override
                        public boolean isModuleSource() {
                            return false;
                        }

                        @Override
                        public DisplayModule createModule(Tab tab) {
                            return null;
                        }

                        @Override
                        public StyledPopupMenu generateMenu(@NotNull TokenContext context) {
                            return null;
                        }

                        @Override
                        public String getIdentifier() {
                            return null;
                        }

                        @Override
                        public boolean equals(ModuleToken other) {
                            return false;
                        }
                    });

                    @Override
                    public String getTitle(TokenContext context) {
                        return projectName;
                    }

                    @Override
                    public String getSubTitle() {
                        return "Project not yet indexed";
                    }

                    @Override
                    public Image getIcon() {
                        return Commons.getIcon("project_unloaded");
                    }

                    @Override
                    public String getHint() {
                        return null;
                    }

                    @Override
                    public Collection<? extends ModuleToken> getSubTokens() {
                        return hintTokens;
                    }

                    @Override
                    public boolean isExpandable() {
                        return true;
                    }

                    @Override
                    public boolean isModuleSource() {
                        return false;
                    }

                    @Override
                    public DisplayModule createModule(Tab tab) {
                        return null;
                    }

                    @Override
                    public StyledPopupMenu generateMenu(@NotNull TokenContext context) {
                        return null;
                    }

                    @Override
                    public String getIdentifier() {
                        return null;
                    }

                    @Override
                    public boolean equals(ModuleToken other) {
                        return false;
                    }

                    @Override
                    public float getAlpha() {
                        return 0.8f;
                    }
                }, explorer, null);
                placeholderItem.setDetailed(true);
                explorer.addElement(placeholderItem);
            }
        }
    }

    @Override
    public String getName() {
        return "TODO";
    }

    @Override
    public String getIconName() {
        return "todo";
    }
}
