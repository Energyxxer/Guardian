package com.energyxxer.guardian.events.events;

import com.energyxxer.guardian.events.GuardianEvent;

import java.nio.file.Path;

public class FileDeletedEvent extends GuardianEvent  {
    public Path oldPath;

    public FileDeletedEvent(Path oldPath) {
        this.oldPath = oldPath;
    }
}
