package com.energyxxer.guardian.ui.editor.completion.snippets;

import com.energyxxer.guardian.global.temp.Lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class SnippetContext {

    private static final HashSet<Lang> languagesWithSnippets = new HashSet<>();

    private static final ArrayList<SnippetContext> values = new ArrayList<>();
    private static final HashMap<String, SnippetContext> saveKeyMap = new HashMap<>();

    public static final SnippetContext EVERYWHERE = new SnippetContext(null, "EVERYWHERE", "Everywhere", null);

    private final Lang lang;
    private final String code;
    private String friendlyName;
    private final String contextTag;

    public SnippetContext(Lang lang, String code, String friendlyName, String contextTag) {
        if(lang != null) {
            code = lang.getCode() + "__" + code;
            friendlyName = friendlyName + " [" + lang.getFriendlyName() + "]";

            languagesWithSnippets.add(lang);
        }

        this.lang = lang;
        this.code = code;
        this.friendlyName = friendlyName;
        this.contextTag = contextTag;

        values.add(this);
        saveKeyMap.put(code, this);
    }

    public static SnippetContext getContextForCode(String code) {
        SnippetContext existing = saveKeyMap.get(code);
        if(existing == null) {
            //SnippetContext for code does not exist. Create a dummy context so this snippet gets saved without changes.
            return new SnippetContext(null, code, code + " (missing language)", null);
        }
        return existing;
    }

    public static void addAliasCode(String code, SnippetContext context) {
        saveKeyMap.put(code, context);
    }

    public String getCode() {
        return code;
    }

    public String getContextTag() {
        return contextTag;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public static ArrayList<SnippetContext> values() {
        return values;
    }

    public static HashSet<Lang> getLanguagesWithSnippets() {
        return languagesWithSnippets;
    }
}
