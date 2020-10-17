package com.energyxxer.guardian.ui.editor.completion.snippets;

import com.energyxxer.commodore.CommandUtils;
import com.energyxxer.guardian.ui.editor.completion.SnippetSuggestion;

import java.util.HashSet;

public class Snippet {
    private boolean enabled = true;
    private String shorthand;
    private String text;
    private String description;
    private HashSet<SnippetContext> contexts = new HashSet<>();
    public boolean expanderApplied = false;

    public Snippet() {
        this(null, null, null);
    }

    public Snippet(String shorthand, String text, String description) {
        this.shorthand = shorthand;
        this.text = text;
        this.description = description;
    }

    public Snippet setContextEnabled(SnippetContext context) {
        contexts.add(context);
        return this;
    }

    public Snippet setContextEnabled(SnippetContext context, boolean enabled) {
        if(enabled) contexts.add(context);
        else contexts.remove(context);
        return this;
    }

    public boolean isEnabledEverywhere() {
        return isContextEnabled(SnippetContext.EVERYWHERE);
    }

    public boolean isContextEnabled(SnippetContext context) {
        return contexts.contains(context);
    }

    public SnippetSuggestion createSuggestion() {
        return new SnippetSuggestion(shorthand, text, description);
    }

    public boolean isContextEnabledForTag(String tag) {
        if(!enabled) return false;
        if(isEnabledEverywhere()) return true;
        if(tag == null) return false;
        for(SnippetContext context : contexts) {
            if(tag.equals(context.getContextTag())) return true;
        }
        return false;
    }

    public String getShorthand() {
        return shorthand;
    }

    public String getText() {
        return text;
    }

    public String getDescription() {
        return description;
    }

    public HashSet<SnippetContext> getContexts() {
        return contexts;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Snippet clone() {
        Snippet cloned = new Snippet(shorthand, text, description);
        cloned.contexts.addAll(this.contexts);
        cloned.enabled = this.enabled;
        return cloned;
    }

    public void setShorthand(String shorthand) {
        this.shorthand = shorthand;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSaveData() {
        StringBuilder sb = new StringBuilder();
        sb.append(enabled ? "e" : "d");
        sb.append(CommandUtils.quote(shorthand));
        sb.append(CommandUtils.quote(description));
        sb.append(CommandUtils.quote(text));
        for(SnippetContext context : contexts) {
            sb.append(CommandUtils.quote(context.getCode()));
        }
        sb.append(';');
        return sb.toString();
    }
}
