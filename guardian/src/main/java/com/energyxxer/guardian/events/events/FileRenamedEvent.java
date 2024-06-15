package com.energyxxer.guardian.events.events;

import com.energyxxer.guardian.events.GuardianEvent;

import java.nio.file.Path;

public class FileRenamedEvent extends GuardianEvent {
    public Path oldPath;
    public Path newPath;

    public FileRenamedEvent(Path oldPath, Path newPath) {
        this.oldPath = oldPath;
        this.newPath = newPath;
    }
}
