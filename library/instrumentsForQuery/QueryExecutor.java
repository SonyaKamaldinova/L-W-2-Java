package library.instrumentsForQuery;

import library.Database;
import library.classes.Author;
import library.classes.Book;

import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.Objects;

public class QueryExecutor {
    private final Database db;
    private final SQLParser parser = new SQLParser();
    public QueryExecutor(Database db) {
        this.db = db;
    }

    public void execute(String sql) {
        String command = removeTrailingSemicolons(sql);
        if (command.isEmpty()) return;
        String upper = command.toUpperCase(Locale.ROOT);
        if (upper.startsWith("INSERT INTO ")) {
            insert(command);
        } else if (upper.startsWith("SELECT ")) {
            select(command);
        } else if (upper.startsWith("UPDATE ")) {
            update(command);
        } else if (upper.startsWith("DELETE FROM ")) {
            delete(command);
        } else if (upper.startsWith("CREATE INDEX ")) {
            createIndex(command);
        } else {
            throw new IllegalArgumentException("Unknown SQL command.");
        }
    }

    private void insert(String sql) {
        String body = removePrefix(sql, "INSERT INTO");
        String table = readWord(body);
        body = body.substring(table.length()).trim();
        List<String> explicitColumns = null;
        if (body.startsWith("(")) {
            int closing = findClosingParenthesis(body, 0);
            explicitColumns = parser.splitComma(body.substring(1, closing));
            body = body.substring(closing + 1).trim();
        }

        body = removePrefix(body, "VALUES");
        if (body.isBlank()) {
            throw new IllegalArgumentException("После VALUES не указаны значения.");
        }

        if (!table.equalsIgnoreCase("Authors") && !table.equalsIgnoreCase("Books")) {
            throw new IllegalArgumentException("Неизвестная таблица: " + table);
        }

        Set<String> allowedColumns = table.equalsIgnoreCase("Authors") ? Set.of("id", "name", "birthdate", "alive") : Set.of("id", "title", "authorid", "price", "publishdate", "available");
        List<String> columns = explicitColumns == null ? defaults(table) : explicitColumns;
        Set<String> normalizedColumns = new HashSet<>();
        for (String column : columns) {
            String normalized = column.trim().toLowerCase(Locale.ROOT);
            if (!allowedColumns.contains(normalized)) {
                throw new IllegalArgumentException("Неизвестный столбец: " + column);
            }

            if (!normalizedColumns.add(normalized)) {
                throw new IllegalArgumentException("Столбец указан несколько раз: " + column);
            }
        }

        List<String> tuples = extractTuples(body);
        for (String t : tuples) {
            List<String> values = parser.splitComma(t);
            if (columns.size() != values.size()) {
                throw new IllegalArgumentException("The number of fields does not match.");
            }

            Map<String, String> row = new HashMap<>();
            for (int i = 0; i < columns.size(); i++) {
                row.put(columns.get(i).toLowerCase(Locale.ROOT), parser.unquote(values.get(i)));
            }

            if (table.equalsIgnoreCase("Authors")) {
                Author author = new Author(integer(row, "id"), required(row, "name"), LocalDate.parse(required(row, "birthdate")), bool(row, "alive"));
                db.insertAuthor(author);
            } else {
                Book book = new Book(integer(row, "id"), required(row, "title"), integer(row, "authorid"), decimal(row, "price"), LocalDate.parse(required(row, "publishdate")), bool(row, "available"));
                db.insertBook(book);
            }
        }

        System.out.println("Record added");
    }

    private List<String> defaults(String table) {
        if (table.equalsIgnoreCase("Authors")) {
            return List.of("id", "name", "birthDate", "alive");
        }

        return List.of("id", "title", "authorId", "price", "publishDate", "available");
    }

    private List<String> extractTuples(String input) {
        List<String> result = new ArrayList<>();
        boolean quoted = false;
        int depth = 0;
        int start = -1;
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\'' && (i == 0 || input.charAt(i - 1) != '\\')) {
                quoted = !quoted;
            }

            if (!quoted) {
                if (c == '(') {
                    if (depth++ == 0) {
                        start = i + 1;
                    }
                } else if (c == ')') {
                    if (--depth < 0) {
                        throw new IllegalArgumentException("Extra closing parenthesis.");
                    }

                    if (depth == 0) {
                        result.add(input.substring(start, i));
                    }
                }
            }
        }

        if (quoted || depth != 0 || result.isEmpty()) {
            throw new IllegalArgumentException("Invalid list VALUES.");
        }

        return result;
    }

    private void select(String sql) {
        String body = removePrefix(sql, "SELECT").trim();
        body = removePrefix(body, "*");
        body = removePrefix(body, "FROM");
        String table = readWord(body);
        body = body.substring(table.length()).trim();
        String whereText = takeOptionalWhere(body);
        if (!body.isBlank() && whereText == null) {
            throw new IllegalArgumentException("Unknown part SELECT: " + body);
        }

        validateTable(table);
        Condition condition = whereText == null ? null : parser.parseCondition(whereText);
        if (table.equalsIgnoreCase("Authors")) {
            List<Author> rows = filterAuthors(condition);
            print(List.of("id", "name", "birthDate", "alive"), rows.stream().map(a -> List.of("" + a.getId(), a.getName(), "" + a.getBirthDate(), "" + a.isAlive())).toList());
        } else {
            List<Book> rows = filterBooks(condition);
            print(List.of("id", "title", "authorId", "price", "publishDate", "available"), rows.stream().map(b -> List.of("" + b.getId(), b.getTitle(), "" + b.getAuthorId(), "" + b.getPrice(), "" + b.getPublishDate(), "" + b.isAvailable())).toList());
        }
    }

    private List<Author> filterAuthors(Condition c) {
        Collection<Author> source = db.getAuthors().values();
        if (c != null && c.operator().equals("=") && db.getIndexes().hasIndex("authors", c.column())) {
            Set<Integer> ids = db.getIndexes().find("authors", c.column(), c.value());
            source = ids.stream().map(db.getAuthors()::get).filter(Objects::nonNull).toList();
            System.out.println("Index used Authors." + c.column());
        }

        List<Author> result = new ArrayList<>();
        for (Author a : source) {
            if (c == null || c.test(db.authorValue(a, c.column()))) {
                result.add(a);
            }
        }

        return result;
    }

    private List<Book> filterBooks(Condition c) {
        Collection<Book> source = db.getBooks().values();
        if (c != null && c.operator().equals("=") && db.getIndexes().hasIndex("books", c.column())) {
            Set<Integer> ids = db.getIndexes().find("books", c.column(), c.value());
            source = ids.stream().map(db.getBooks()::get).filter(Objects::nonNull).toList();
            System.out.println("Index used Books." + c.column());
        }

        List<Book> result = new ArrayList<>();
        for (Book b : source) {
            if (c == null || c.test(db.bookValue(b, c.column()))) {
                result.add(b);
            }
        }

        return result;
    }

    private void update(String sql) {
        String body = removePrefix(sql, "UPDATE");
        String table = readWord(body);
        body = body.substring(table.length()).trim();
        body = removePrefix(body, "SET");
        String whereText = takeOptionalWhere(body);
        String assignments = whereText == null ? body : body.substring(0, findKeywordOutsideQuotes(body, "WHERE")).trim();
        validateTable(table);
        Set<String> allowedColumns = table.equalsIgnoreCase("Authors") ? Set.of("name", "birthdate", "alive") : Set.of("title", "authorid", "price", "publishdate", "available");
        Map<String, String> changes = new HashMap<>();
        for (String assignment : parser.splitComma(assignments)) {
            String[] pair = assignment.split("=", 2);
            if (pair.length != 2) {
                throw new IllegalArgumentException("Incorrect assignment: " + assignment);
            }

            changes.put(pair[0].trim().toLowerCase(Locale.ROOT), parser.unquote(pair[1]));
        }

        for (String c : changes.keySet()) {
            if (!allowedColumns.contains(c)) {
                throw new IllegalArgumentException("Modifying an unknown or immutable column is not permitted.: " + c);
            }
        }

        Condition condition = whereText == null ? null : parser.parseCondition(whereText);
        int count = 0;
        if (table.equalsIgnoreCase("Authors")) {
            for (Author a : new ArrayList<>(db.getAuthors().values())) {
                if (!matchesAuthor(a, condition)) continue;
                Author updated = new Author(a.getId(), changes.getOrDefault("name", a.getName()), LocalDate.parse(changes.getOrDefault("birthdate", "" + a.getBirthDate())), Boolean.parseBoolean(changes.getOrDefault("alive", "" + a.isAlive())));
                db.updateAuthor(a, updated);
                count++;
            }
        } else {
            for (Book b : new ArrayList<>(db.getBooks().values())) {
                if (!matchesBook(b, condition)) continue;
                Book updated = new Book(b.getId(), changes.getOrDefault("title", b.getTitle()), Integer.parseInt(changes.getOrDefault("authorid", "" + b.getAuthorId())), Double.parseDouble(changes.getOrDefault("price", "" + b.getPrice())), LocalDate.parse(changes.getOrDefault("publishdate", "" + b.getPublishDate())), Boolean.parseBoolean(changes.getOrDefault("available", "" + b.isAvailable())));
                db.updateBook(b, updated);
                count++;
            }
        }

        System.out.println("Records updated: " + count);
    }

    private void delete(String sql) {
        String body = removePrefix(sql, "DELETE");
        body = removePrefix(body, "FROM");
        String table = readWord(body);
        body = body.substring(table.length()).trim();
        String whereText = takeOptionalWhere(body);
        if (!body.isBlank() && whereText == null) {
            throw new IllegalArgumentException("Unknown part DELETE: " + body);
        }

        validateTable(table);
        Condition condition = whereText == null ? null : parser.parseCondition(whereText);
        int count = 0;
        if (table.equalsIgnoreCase("Authors")) {
            List<Integer> ids = db.getAuthors().values().stream().filter(a -> matchesAuthor(a, condition)).map(Author::getId).toList();
            for (int id : ids) {
                db.deleteAuthor(id);
                count++;
            }
        } else {
            List<Integer> ids = db.getBooks().values().stream().filter(b -> matchesBook(b, condition)).map(Book::getId).toList();
            for (int id : ids) {
                db.deleteBook(id);
                count++;
            }
        }

        System.out.println("Records deleted: " + count);
    }

    private void createIndex(String sql) {
        String body = removePrefix(sql, "CREATE");
        body = removePrefix(body, "INDEX");
        String indexName = readWord(body);
        body = body.substring(indexName.length()).trim();
        body = removePrefix(body, "ON");
        String table = readWord(body);
        body = body.substring(table.length()).trim();
        if (!body.startsWith("(") || !body.endsWith(")")) {
            throw new IllegalArgumentException("Format: CREATE INDEX idx ON Books(title);");
        }

        String column = body.substring(1, body.length() - 1).trim();
        if (column.isBlank() || column.contains(",") || column.contains(" ")) {
            throw new IllegalArgumentException("Specify one valid index column.");
        }

        validateTable(table);
        db.createIndex(indexName, table, column);
        System.out.println("The index has been created and saved.");
    }

    private boolean matchesAuthor(Author a, Condition c) {
        return c == null || c.test(db.authorValue(a, c.column()));
    }

    private boolean matchesBook(Book b, Condition c) {
        return c == null || c.test(db.bookValue(b, c.column()));
    }

    private int integer(Map<String, String> row, String key) {
        return Integer.parseInt(required(row, key));
    }

    private double decimal(Map<String, String> row, String key) {
        return Double.parseDouble(required(row, key));
    }

    private boolean bool(Map<String, String> row, String key) {
        String value = required(row, key);
        if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            throw new IllegalArgumentException(key + " must have a value of true or false.");
        }

        return Boolean.parseBoolean(value);
    }

    private String required(Map<String, String> row, String key) {
        String v = row.get(key);
        if (v == null || v.isBlank()) {
            throw new IllegalArgumentException("Required field is not set: " + key);
        }

        return v;
    }

    private void print(List<String> headers, List<List<String>> rows) {
        List<Integer> widths = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            int width = headers.get(i).length();
            for (List<String> r : rows) {
                width = Math.max(width, r.get(i).length());
            }
            widths.add(Math.min(width, 32));
        }
        String border = "+" + widths.stream().map(w -> "-".repeat(w + 2)).reduce((a, b) -> a + "+" + b).orElse("") + "+";
        System.out.println(border);
        printRow(headers, widths);
        System.out.println(border);
        for (List<String> r : rows) {
            printRow(r, widths);
        }

        System.out.println(border);
        System.out.println("Rows: " + rows.size());
    }

    private void printRow(List<String> cells, List<Integer> widths) {
        StringBuilder line = new StringBuilder("|");
        for (int i = 0; i < cells.size(); i++) {
            String v = cells.get(i);
            if (v.length() > widths.get(i)) {
                v = v.substring(0, widths.get(i) - 1) + "…";
            }

            line.append(" ").append(String.format("%-" + widths.get(i) + "s", v)).append(" |");
        }
        System.out.println(line);
    }

    private String removeTrailingSemicolons(String sql) {
        if (sql == null) {
            throw new IllegalArgumentException("Команда не может быть null.");
        }
        String result = sql.trim();
        while (result.endsWith(";")) {
            result = result.substring(0, result.length() - 1).trim();
        }
        return result;
    }

    private String removePrefix(String text, String keyword) {
        String value = text.trim();
        if (!value.regionMatches(true, 0, keyword, 0, keyword.length())) {
            throw new IllegalArgumentException("Keyword expected " + keyword);
        }

        if (value.length() > keyword.length() && !Character.isWhitespace(value.charAt(keyword.length())) && value.charAt(keyword.length()) != '*' && value.charAt(keyword.length()) != '(') {
            throw new IllegalArgumentException("Invalid keyword " + keyword);
        }

        return value.substring(keyword.length()).trim();
    }

    private String readWord(String text) {
        String value = text.trim();
        int end = 0;
        while (end < value.length() && !Character.isWhitespace(value.charAt(end)) && value.charAt(end) != '(') {
            end++;
        }

        if (end == 0) {
            throw new IllegalArgumentException("Table or index name not specified.");
        }

        return value.substring(0, end);
    }

    private String takeOptionalWhere(String text) {
        int wherePosition = findKeywordOutsideQuotes(text, "WHERE");
        if (wherePosition < 0) return null;
        return text.substring(wherePosition + "WHERE".length()).trim();
    }

    private int findKeywordOutsideQuotes(String text, String keyword) {
        boolean quoted = false;
        for (int i = 0; i <= text.length() - keyword.length(); i++) {
            char current = text.charAt(i);
            if (current == '\'') {
                if (quoted && i + 1 < text.length() && text.charAt(i + 1) == '\'') {
                    i++;
                    continue;
                }

                quoted = !quoted;
                continue;
            }

            if (!quoted && text.regionMatches(true, i, keyword, 0, keyword.length())) {
                boolean leftBoundary = i == 0 || Character.isWhitespace(text.charAt(i - 1));
                int after = i + keyword.length();
                boolean rightBoundary = after == text.length() || Character.isWhitespace(text.charAt(after));
                if (leftBoundary && rightBoundary) return i;
            }
        }

        return -1;
    }

    private int findClosingParenthesis(String text, int openingPosition) {
        boolean quoted = false;
        int depth = 0;
        for (int i = openingPosition; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == '\'') {
                if (quoted && i + 1 < text.length() && text.charAt(i + 1) == '\'') {
                    i++;
                    continue;
                }
                quoted = !quoted;
            } else if (!quoted) {
                if (current == '(') depth++;
                if (current == ')' && --depth == 0) return i;
            }
        }

        throw new IllegalArgumentException("Closing parenthesis not found.");
    }

    private void validateTable(String table) {
        if (!table.equalsIgnoreCase("Authors") && !table.equalsIgnoreCase("Books")) {
            throw new IllegalArgumentException("Unknown table: " + table);
        }
    }

}