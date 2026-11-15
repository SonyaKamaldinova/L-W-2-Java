package library.instrumentsForQuery;

/**
 * A condition for comparing a column value with an SQL literal.
 * @param column   the column name
 * @param operator the comparison operator
 * @param value    the value to compare against
 */
public record Condition(String column, String operator, String value) {
    /**
     * Checks whether the value satisfies the condition.
     */
    public boolean test(String actual) {
        int comparison;
        try {
            comparison = Double.compare(Double.parseDouble(actual), Double.parseDouble(value));
        } catch (NumberFormatException e) {
            comparison = actual.compareToIgnoreCase(value);
        }

        switch (operator) {
            case "=":
                return actual.equalsIgnoreCase(value);
            case "!=" , "<>":
                    return !actual.equalsIgnoreCase(value);
            case ">":
                return comparison > 0;
            case "<":
                return comparison < 0;
            case ">=":
                return comparison >= 0;
            case "<=":
                return comparison <= 0;
            default:
                throw new IllegalArgumentException("Unknown operator: " + operator);
        }
    }
}