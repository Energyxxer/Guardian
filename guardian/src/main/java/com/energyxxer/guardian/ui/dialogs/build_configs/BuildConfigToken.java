package com.energyxxer.guardian.ui.dialogs.build_configs;

import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.global.temp.projects.BuildConfiguration;
import com.energyxxer.guardian.main.window.GuardianWindow;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.orderlist.CompoundActionModuleToken;
import com.energyxxer.guardian.ui.orderlist.ItemAction;
import com.energyxxer.guardian.ui.styledcomponents.StyledPopupMenu;
import com.energyxxer.prismarine.util.JsonTraverser;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class BuildConfigToken implements CompoundActionModuleToken {

    public String name;
    public File file;
    public JsonObject root;
    public JsonTraverser traverser;

    public BuildConfigToken(BuildConfiguration<?> config) {
        this.name = config.name;
        this.file = config.file;
        this.root = config.root.deepCopy();
        this.traverser = new JsonTraverser(root);
    }

    public BuildConfigToken(String name, File directory, JsonObject root) {
        this.name = name;
        this.file = directory.toPath().resolve(new Random().nextInt(Integer.MAX_VALUE) + ".build").toFile();
        this.root = root;
        this.traverser = new JsonTraverser(root);
    }

    @Override
    public @NotNull List<ItemAction> getActions() {
        return Collections.emptyList();
    }

    @Override
    public String getTitle(TokenContext context) {
        return name;
    }

    @Override
    public Image getIcon() {
        return Commons.getIcon("cog");
    }

    @Override
    public StyledPopupMenu generateMenu(@NotNull TokenContext context) {
        return null;
    }

    @Override
    public boolean equals(ModuleToken other) {
        return this == other;
    }

    public void writeToFile(Gson gson) {
        traverser.reset().createOnTraversal().get("guardian").get("name").set(name);

        try(PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            writer.print(gson.toJson(root));
        } catch (FileNotFoundException | UnsupportedEncodingException x) {
            x.printStackTrace();
            GuardianWindow.showException(x);
        }
    }

    public void setSortIndex(int index) {
        traverser.reset().createOnTraversal().get("guardian").get("sort-index").set(index);
    }
}
