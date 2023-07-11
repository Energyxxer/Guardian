package com.energyxxer.guardian.ui.editor.behavior;

import com.energyxxer.util.StringUtil;

import javax.swing.text.AttributeSet;
import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Pattern;

public class IndentationManager {
    public static final String FORCE_BRACE_STYLE = "__BRACE_FORCE";
    public static final String NULLIFY_BRACE_STYLE = "__INDENTATION_CANCEL";
    protected final AdvancedEditor editor;
    protected boolean dirty = false;
    protected String text;
    private String openingChars = "{[(";
    private String closingChars = "}])";
    private String weakOpeningChars = "";
    private String weakClosingChars = "";

    protected final ArrayList<IndentationChange> indents = new ArrayList<>();
    private final Stack<Integer> bracesSeen = new Stack<>();
    private boolean pairsSyntaxDriven = false;

    public IndentationManager(AdvancedEditor editor) {
        this.editor = editor;

        editor.getStyledDocument().addStyle(FORCE_BRACE_STYLE, null);
        editor.getStyledDocument().addStyle(NULLIFY_BRACE_STYLE, null);

        setBraceSet("{[(","}])");
    }

    public void textChanged(String newText) {
        this.text = newText;
        dirty = true;
        indents.clear();
        bracesSeen.empty();
    }

    public int getSuggestedIndentationLevelAt(int index) {
        populate();

        int level = 0;
        for(IndentationChange indent : indents) {
            if(!isRealIndent(indent)) continue;
            if(index <= indent.index) {
                return level;
            }
            level += indent.change;
        }
        return level;
    }

    private void populate() {
        if(!dirty) return;
        bracesSeen.empty();
        for(int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int openingIndex = openingChars.indexOf(c);
            int closingIndex = closingChars.indexOf(c);

            boolean strong = isStrongBrace(c);

            if(openingIndex >= 0) {
                //bracesSeen.push(openingIndex);
                indents.add(new IndentationChange(i, +1, strong));
            } else if(closingIndex >= 0) {
                /*int matchingBraceIndex = closingIndex;
                if(!bracesSeen.isEmpty()) {
                    matchingBraceIndex = bracesSeen.pop();
                }*/
                indents.add(new IndentationChange(i, -1, strong));
            }
        }
        dirty = false;
    }

    private boolean isStrongBrace(char c) {
        return !weakOpeningChars.contains(""+c) && !weakClosingChars.contains(""+c);
    }

    public boolean isBalanced() {
        return text == null || getSuggestedIndentationLevelAt(text.length()) == 0;
    }

    public boolean match(char opening, char closing) {
        return isOpeningBrace(opening) && openingChars.indexOf(opening) == closingChars.indexOf(closing);
    }

    public boolean isOpeningBrace(String str) {
        return str.length() == 1 && isOpeningBrace(str.charAt(0));
    }

    public boolean isOpeningBrace(char ch) {
        return openingChars.indexOf(ch) >= 0;
    }

    public boolean isClosingBrace(String str) {
        return str.length() == 1 && isClosingBrace(str.charAt(0));
    }

    public boolean isClosingBrace(char ch) {
        return closingChars.indexOf(ch) >= 0;
    }

    public char getMatchingBraceChar(String str) {
        return getMatchingBraceChar(str.charAt(0));
    }

    public char getMatchingBraceChar(char ch) {
        if(isOpeningBrace(ch)) return closingChars.charAt(openingChars.indexOf(ch));
        return openingChars.charAt(closingChars.indexOf(ch));
    }

    public boolean isBrace(char ch) {
        return isOpeningBrace(ch) || isClosingBrace(ch);
    }

    public IndentationManager setBraceSet(String openingBraces, String closingBraces) {
        this.openingChars = openingBraces;
        this.closingChars = closingBraces;
        textChanged(editor.getText());

        braceMatcher = Pattern.compile("[" + Pattern.quote(openingBraces + closingBraces) + "]");

        return this;
    }
    public IndentationManager setWeakBraces(String openingBraces, String closingBraces) {
        this.weakOpeningChars = openingBraces;
        this.weakClosingChars = closingBraces;
        return this;
    }

    public IndentationManager setPairCompletionSyntaxDriven(boolean syntaxDriven) {
        this.pairsSyntaxDriven = syntaxDriven;
        return this;
    }

    public boolean isPairCompletionSyntaxDriven() {
        return pairsSyntaxDriven;
    }

    private int binarySearchBraceIndex(int index) {
        int min = 0;
        int max = indents.size()-1;
        while(true) {

            if(min >= max) {
                if(!indents.isEmpty() && indents.get(min).index == index && isRealIndent(indents.get(min))) return min;
                return -1;
            }

            int pivotIndex = (min + max) / 2;
            IndentationChange pivot = indents.get(pivotIndex);
            if(index == pivot.index && isRealIndent(pivot)) return pivotIndex;
            else if(index < pivot.index) {
                max = pivotIndex-1;
            } else {
                min = pivotIndex+1;
            }
        }
    }

    private boolean isRealIndent(IndentationChange indent) {
        AttributeSet characterAttributes = editor.getStyledDocument().getCharacterElement(indent.index).getAttributes();
        return !characterAttributes.containsAttributes(editor.getStyle(NULLIFY_BRACE_STYLE))
                && !characterAttributes.containsAttributes(editor.getStyle(AdvancedEditor.STRING_STYLE))
                && (indent.strong || characterAttributes.containsAttributes(editor.getStyle(FORCE_BRACE_STYLE)));
    }

    public int getMatchingBraceIndex(int braceCheckIndex) {
        populate();
        int indentIndex = binarySearchBraceIndex(braceCheckIndex);
        if(indentIndex == -1) return -1;
        IndentationChange brace = indents.get(indentIndex);
        int level = 1;
        for(int i = indentIndex + brace.change; i >= 0 && i < indents.size(); i += brace.change) {
            IndentationChange next = indents.get(i);
            if(!isRealIndent(next)) continue;
            level += next.change * brace.change;
            if(level == 0) return next.index;
        }
        return -1;
    }

    private Pattern braceMatcher;

    public Pattern getBraceMatcher() {
        return braceMatcher;
    }

    public String indent(int level) {
        return StringUtil.repeat(" ", editor.tabSize*level);
    }

    private static class IndentationChange {
        int index;
        int change;
        boolean strong;

        public IndentationChange(int index, int change, boolean strong) {
            this.index = index;
            this.change = change;
            this.strong = strong;
        }

        @Override
        public String toString() {
            return "IndentationChange{" +
                    "index=" + index +
                    ", change=" + change +
                    '}';
        }
    }
}
