package com.energyxxer.guardian.ui.editor.completion;

import com.energyxxer.enxlex.suggestions.LiteralSuggestion;
import com.energyxxer.enxlex.suggestions.Suggestion;
import com.energyxxer.guardian.global.Commons;
import com.energyxxer.guardian.global.temp.Lang;
import com.energyxxer.guardian.ui.Tab;
import com.energyxxer.guardian.ui.display.DisplayModule;
import com.energyxxer.guardian.ui.editor.EditorModule;
import com.energyxxer.guardian.ui.modules.ModuleToken;
import com.energyxxer.guardian.ui.styledcomponents.StyledPopupMenu;
import com.energyxxer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collection;
import java.util.Locale;

public class ExpandableSuggestionToken implements SuggestionToken, ModuleToken {

    protected SuggestionDialog parent;
    protected String preview;
    protected String text;
    protected String description;
    protected Suggestion suggestion;

    protected String iconKey;

    protected boolean enabled = true;
    protected int backspaces = 0;
    protected int endIndex = -1;

    protected float alpha = 1f;

    protected boolean caseSensitive = false;

    protected ExpandableSuggestionToken() {

    }

    public ExpandableSuggestionToken(SuggestionDialog parent, String text, Suggestion suggestion) {
        this(parent, text, text, suggestion);
    }

    public ExpandableSuggestionToken(SuggestionDialog parent, String preview, String text, Suggestion suggestion) {
        this.parent = parent;
        this.suggestion = suggestion;
        this.preview = preview;
        this.text = text;

        EditorModule editorModule = parent.getEditor().getParentModule();
        Lang lang = editorModule != null ? editorModule.getLanguage() : null;

        if(lang != null) {
            iconKey = lang.getIconKeyForSuggestionTags(suggestion.getTags());
        }

        if(suggestion instanceof SnippetSuggestion) {
            description = "  " + ((SnippetSuggestion) suggestion).getDescription();
        }

        if(suggestion instanceof LiteralSuggestion) {
            this.caseSensitive = ((LiteralSuggestion) suggestion).isCaseSensitive();
        }
    }

    public String getIconKey() {
        return iconKey;
    }

    public void setIconKey(String iconKey) {
        this.iconKey = iconKey;
    }

    @Override
    public String getTitle(TokenContext context) {
        return preview;
    }

    @Override
    public String getSubTitle() {
        return description;
    }

    @Override
    public Image getIcon() {
        return iconKey != null ? Commons.getIcon(iconKey) : null;
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

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
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
    public void onInteract() {
        parent.submit(StringUtil.repeat("\b", backspaces) + text, suggestion, true, endIndex);
    }

    @Override
    public int getDefaultXOffset() {
        return -25;
    }

    @Override
    public StyledPopupMenu generateMenu(@NotNull ModuleToken.TokenContext context) {
        return null;
    }

    @Override
    public String getIdentifier() {
        return null;
    }

    @Override
    public boolean equals(ModuleToken other) {
        return other instanceof ExpandableSuggestionToken && ((ExpandableSuggestionToken) other).text.equals(this.text);
    }

    public void setEnabledFilter(String filter) {
        enabled = filter.isEmpty() || (caseSensitive ? this.preview.startsWith(filter) : this.preview.toLowerCase(Locale.ENGLISH).startsWith(filter.toLowerCase(Locale.ENGLISH)));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setBackspaces(int backspaces) {
        this.backspaces = backspaces;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getBackspaces() {
        return backspaces;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    @Override
    public float getAlpha() {
        return alpha;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    @Override
    public String toString() {
        return preview;
    }
}
