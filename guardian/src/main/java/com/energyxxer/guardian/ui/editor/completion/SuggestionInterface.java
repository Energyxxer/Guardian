package com.energyxxer.guardian.ui.editor.completion;

public interface SuggestionInterface {
    void dismiss(boolean force);
    void relocate();

    void lock();
    void setSafeToSuggest(boolean safe);
}
