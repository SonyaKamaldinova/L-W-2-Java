package library;

import library.classes.Author;
import library.classes.Book;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
/**
 * A serializer that converts tables into human-readable CSV files.
 * Writing is performed via a temporary file, which then replaces the original.
 */
public class SaveManager {
    /**
     * Loads authors from a CSV file.
     * @throws IOException in case of a format or reading error.
     */
    public List<Author> loadAuthors(Path file) throws IOException {
        List<Author> authors = new ArrayList<>();
        if (!Files.exists(file)) {
            return authors;
        }

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) continue;
            List<String> fields = parseCsvLine(lines.get(i));
            if (fields.size() != 4) {
                throw new IOException("Error in authors.csv, line " + (i + 1));
            }

            try {
                authors.add(new Author(Integer.parseInt(fields.get(0)), fields.get(1), LocalDate.parse(fields.get(2)), Boolean.parseBoolean(fields.get(3))));
            } catch (RuntimeException e) {
                throw new IOException("Incorrect author data in the line " + (i + 1), e);
            }
        }

        return authors;
    }
    /**
     * Loads books from a CSV file.
     * @throws IOException if a format or reading error occurs.
     */
    public List<Book> loadBooks(Path file) throws IOException {
        List<Book> books = new ArrayList<>();
        if (!Files.exists(file)) {
            return books;
        }

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            if (lines.get(i).isBlank()) continue;
            List<String> fields = parseCsvLine(lines.get(i));
            if (fields.size() != 6) {
                throw new IOException("Error in books.csv, line " + (i + 1));
            }

            try {
                books.add(new Book(Integer.parseInt(fields.get(0)), fields.get(1), Integer.parseInt(fields.get(2)), Double.parseDouble(fields.get(3)), LocalDate.parse(fields.get(4)), Boolean.parseBoolean(fields.get(5))));
            } catch (RuntimeException e) {
                throw new IOException("Incorrect book data in the line " + (i + 1), e);
            }
        }

        return books;
    }
    /**
     * Saves authors to a CSV file.
     * @throws IOException if an error occurs during writing.
     */
    public void saveAuthors(Path file, Iterable<Author> authors) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("id, name, birthDate, alive");
        for (Author a : authors) {
            lines.add(String.join(",", csv(String.valueOf(a.getId())), csv(a.getName()), csv(a.getBirthDate().toString()), csv(String.valueOf(a.isAlive()))));
        }

        writeAtomically(file, lines);
    }
    /**
     * Saves books to CSV.
     * @throws IOException if a writing error occurs.
     */
    public void saveBooks(Path file, Iterable<Book> books) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("id, title, authorId, price, publishDate, available");
        for (Book b : books) {
            lines.add(String.join(",", csv(String.valueOf(b.getId())), csv(b.getTitle()), csv(String.valueOf(b.getAuthorId())), csv(String.valueOf(b.getPrice())), csv(b.getPublishDate().toString()), csv(String.valueOf(b.isAvailable()))));
        }

        writeAtomically(file, lines);
    }
    /**
     * Loads the list of indexed columns.
     * @throws IOException if a reading error occurs.
     */
    public Set<String> loadIndexedColumns(Path file) throws IOException {
        Set<String> columns = new TreeSet<>();
        if (!Files.exists(file)) {
            return columns;
        }

        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        for (int i = 1; i < lines.size(); i++) {
            if (!lines.get(i).isBlank()) {
                columns.add(lines.get(i).trim());
            }
        }

        return columns;
    }
    /**
     * Saves the list of indexed columns.
     * @throws IOException if a write error occurs.
     */
    public void saveIndexedColumns(Path file, Set<String> columns) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add("table.column");
        for (String c : columns) {
            lines.add(csv(c));
        }

        writeAtomically(file, lines);
    }

    private void writeAtomically(Path file, List<String> lines) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.write(temp, lines, StandardCharsets.UTF_8);

        try {
            Files.move(temp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(temp, file, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String csv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private List<String> parseCsvLine(String line) throws IOException {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean quoted = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (quoted && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    quoted = !quoted;
                }
            } else if (c == ',' && !quoted) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        if (quoted) {
            throw new IOException("Unclosed quotation mark in a CSV string: " + line);
        }

        fields.add(current.toString());
        return fields;
    }
}