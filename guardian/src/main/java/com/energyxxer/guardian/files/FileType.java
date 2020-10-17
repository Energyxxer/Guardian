package com.energyxxer.guardian.files;

import com.energyxxer.guardian.ui.dialogs.file_dialogs.FileDialog;
import com.energyxxer.guardian.ui.dialogs.file_dialogs.FolderDialog;
import com.energyxxer.guardian.ui.styledcomponents.StyledMenuItem;
import com.energyxxer.prismarine.util.SortedList;

import java.util.function.Predicate;

public class FileType {
    private static final SortedList<FileType> values = new SortedList<>(f -> f.group);

    public static FileType JSON = new FileType(10, "JSON File", "json", ".json", FileDialog::create, (pr, pth) -> true);
    public static FileType FILE = new FileType(20, "File", "file", "", FileDialog::create, (pr, pth) -> true);
    public static FileType FOLDER = new FileType(20, "Folder", "folder", null, FolderDialog::create, (pr, pth) -> true);

    public final int group;
    public final String name;
    public final String icon;
    public final String extension;
    public final FileTypeDialog dialog;
    public final DirectoryValidator validator;
    public final Predicate<String> fileNameValidator;

    public FileType(int group, String name, String icon, String extension, FileTypeDialog dialog, DirectoryValidator validator) {
        this(group, name, icon, extension, dialog, validator, s -> true);
    }

    public FileType(int group, String name, String icon, String extension, FileTypeDialog dialog, DirectoryValidator validator, Predicate<String> fileNameValidator) {
        this.group = group;
        this.name = name;
        this.icon = icon;
        this.extension = extension;
        this.dialog = dialog;
        this.validator = validator;
        this.fileNameValidator = fileNameValidator;

        values.add(this);
    }

    public void create(String destination) {
        this.dialog.create(this, destination);
    }

    public StyledMenuItem createMenuItem(String newPath) {
        StyledMenuItem item = new StyledMenuItem(name, icon);
        item.addActionListener(e -> create(newPath));
        return item;
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean canCreate(String projectDir, String path) {
        return validator.canCreate(projectDir, path);
    }

    public static Iterable<FileType> values() {
        return values;
    }
}
