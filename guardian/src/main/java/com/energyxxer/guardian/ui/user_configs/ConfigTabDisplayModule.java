package com.energyxxer.guardian.ui.user_configs;

import com.energyxxer.guardian.global.temp.projects.Project;
import com.energyxxer.guardian.ui.dialogs.build_configs.FieldHost;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.scrollbar.OverlayScrollPane;
import com.energyxxer.guardian.ui.styledcomponents.Padding;
import com.energyxxer.guardian.ui.styledcomponents.StyledLabel;
import com.energyxxer.guardian.ui.theme.change.ThemeListenerManager;
import com.energyxxer.prismarine.util.JsonTraverser;
import com.energyxxer.util.Disposable;
import com.energyxxer.xswing.ScalableDimension;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ConfigTabDisplayModule extends JPanel implements DisplayModule, Disposable, FieldHost<JsonTraverser> {
    private ThemeListenerManager tlm = new ThemeListenerManager();
    private ArrayList<Consumer<JsonTraverser>> openEvents = new ArrayList<>();
    private ArrayList<Consumer<JsonTraverser>> applyEvents = new ArrayList<>();

    public ConfigTabDisplayModule(ConfigTab configTab, Project project) {
        this.setOpaque(false);
        this.setLayout(new BorderLayout());

        JPanel margin = new JPanel(new BorderLayout());
        margin.setOpaque(false);

        margin.add(new Padding(20), BorderLayout.NORTH);
        margin.add(new Padding(50), BorderLayout.WEST);
        margin.add(new Padding(50), BorderLayout.EAST);
        margin.add(new Padding(20), BorderLayout.SOUTH);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        margin.add(content, BorderLayout.CENTER);

        OverlayScrollPane scrollPane = new OverlayScrollPane(tlm, margin);
        this.add(scrollPane, BorderLayout.CENTER);

        {
            JPanel header = new JPanel(new BorderLayout());
            header.setPreferredSize(new ScalableDimension(0,40));
            this.add(header, BorderLayout.NORTH);

            {
                JPanel padding = new JPanel();
                padding.setOpaque(false);
                padding.setPreferredSize(new ScalableDimension(25,25));
                header.add(padding, BorderLayout.WEST);
            }

            StyledLabel label = new StyledLabel(configTab.getTitle(), "ProjectProperties.content.header", tlm);
            header.add(label, BorderLayout.CENTER);

            tlm.addThemeChangeListener(t -> {
                setBackground(t.getColor(new Color(235, 235, 235), "ProjectProperties.content.background"));
                header.setBackground(t.getColor(new Color(235, 235, 235), "ProjectProperties.content.header.background"));
                header.setBorder(BorderFactory.createMatteBorder(0, 0, Math.max(t.getInteger(1,"ProjectProperties.content.header.border.thickness"),0), 0, t.getColor(new Color(200, 200, 200), "ProjectProperties.content.header.border.color")));
            });
        }

        for(ConfigTabDisplayModuleEntry entry : configTab.getEntries()) {
            entry.create(tlm, content, this, project);
        }
    }

    public void addOpenEvent(Consumer<JsonTraverser> event) {
        this.openEvents.add(event);
    }

    public void addApplyEvent(Consumer<JsonTraverser> event) {
        this.applyEvents.add(event);
    }

    @Override
    public void displayCaretInfo() {

    }

    @Override
    public Object getValue() {
        return null;
    }

    @Override
    public boolean canSave() {
        return false;
    }

    @Override
    public Object save() {
        return null;
    }

    @Override
    public void focus() {
        this.requestFocus();
    }

    @Override
    public boolean moduleHasFocus() {
        return this.isFocusOwner();
    }

    @Override
    public boolean transform(ModuleToken newToken) {
        return false;
    }

    @Override
    public void dispose() {
        tlm.dispose();
    }

    public void apply(JsonTraverser traverser) {
        for(Consumer<JsonTraverser> event : applyEvents) {
            event.accept(traverser.reset());
        }
    }

    public void open(JsonTraverser config) {
        for(Consumer<JsonTraverser> event : openEvents) {
            event.accept(config.reset());
        }
    }
}
