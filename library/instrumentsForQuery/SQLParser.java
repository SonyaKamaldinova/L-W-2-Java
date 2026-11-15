package library.instrumentsForQuery;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * A parser for SQL-like commands.
 */
public class SQLParser {
    private static final Pattern CONDITION_PATTERN = Pattern.compile("\\s*" + "(?<column>\\w+)" + "\\s*" +"(?<operator>=|!=|<>|>=|<=|>|<)" + "\\s*" +"(?<value>.+?)" + "\\s*", Pattern.DOTALL);
    /**
     * Parses the WHERE clause.
     */
    public Condition parseCondition(String text) {
        Matcher matcher = CONDITION_PATTERN.matcher(text);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Incorrect condition WHERE: " + text);
        }

        return new Condition(matcher.group(1), matcher.group(2), unquote(matcher.group(3)));
    }
    /**
     * Splits a string by commas,
     * without splitting values enclosed in quotation marks.
     */
    public List<String> splitComma(String input) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\'' && (i == 0 || input.charAt(i - 1) != '\\')) {
                quoted = !quoted;
            }

            if (c == ',' && !quoted) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        if (quoted) {
            throw new IllegalArgumentException("A quoted string is not closed.");
        }

        if (!current.isEmpty()) {
            result.add(current.toString().trim());
        }

        return result;
    }
    /**
     * Removes the quotation marks around a string value.
     */
    public String unquote(String value) {
        String result = value.trim();
        if (result.endsWith(";")) {
            result = result.substring(0, result.length() - 1).trim();
        }

        if (result.length() >= 2 && result.startsWith("'") && result.endsWith("'")) {
            return result.substring(1, result.length() - 1).replace("''", "'");
        }

        return result;
    }
}