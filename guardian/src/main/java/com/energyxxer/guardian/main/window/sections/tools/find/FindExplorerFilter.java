package com.energyxxer.guardian.main.window.sections.tools.find;

public interface FindExplorerFilter {
    boolean groupByProject();
    boolean groupBySubProject();
    boolean groupByPath();
    boolean groupByFile();

    default boolean highlightResult() {
        return true;
    }
}
