package com.energyxxer.guardian.ui.editor.completion;

public class ParameterNameSuggestionToken implements SuggestionToken {
    private final String parameterName;

    public ParameterNameSuggestionToken(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getParameterName() {
        return parameterName;
    }

    @Override
    public String toString() {
        return "Param: (" + parameterName + ")";
    }
}
