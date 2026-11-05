package base;

import java.time.LocalDate;

public enum DataType {

    INTEGER,
    DOUBLE,
    STRING,
    DATE,
    BOOLEAN;

    public Object parse(String v){
        switch (this) {
            case INTEGER:
                return Integer.parseInt(v);

            case DOUBLE:
                return Double.parseDouble(v);

            case STRING:
                return v;

            case DATE:
                return LocalDate.parse(v);

            case BOOLEAN:
                if (!v.equalsIgnoreCase("true") && !v.equalsIgnoreCase("false")) {
                    throw new IllegalArgumentException("Invalid boolean: " + v);
                }
                return Boolean.parseBoolean(v);

            default:
                return -1;
        }
    }
}
