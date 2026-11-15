package library;

import library.dao.AuthorsTableDao;
import library.dao.BooksTableDao;
import library.dao.TableDao;
import library.IndexManager;
import library.classes.Author;
import library.classes.Book;
import library.SaveManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Coordinates tables, relationship constraints, indexes, and data persistence.
 * Uses DAOs to decouple database logic from the storage format.
 */
public class Database {
    private final SaveManager storage = new SaveManager();
    private final TableDao<Author> authorDao = new AuthorsTableDao(storage);
    private final TableDao<Book> bookDao = new BooksTableDao(storage);
    private final Path dataDirectory;
    private final Path authorsFile;
    private final Path booksFile;
    private final Path indexesFile;
    private Map<Integer, Author> authors = new HashMap<>();
    private Map<Integer, Book> books = new HashMap<>();
    private IndexManager indexes = new IndexManager();
    /**
     * Opens the database from the specified folder and rebuilds the indexes.
     */
    public Database(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.authorsFile = dataDirectory.resolve("authors.csv");
        this.booksFile = dataDirectory.resolve("books.csv");
        this.indexesFile = dataDirectory.resolve("indexes.csv");

        try {
            Files.createDirectories(dataDirectory);
            for (Author a : authorDao.load(authorsFile)) {
                if (authors.putIfAbsent(a.getId(), a) != null) {
                    throw new IOException("Author ID is repeated: " + a.getId());
                }
            }

            for (Book b : bookDao.load(booksFile)) {
                if (books.putIfAbsent(b.getId(), b) != null) {
                    throw new IOException("Book ID is repeated: " + b.getId());
                }

                if (!authors.containsKey(b.getAuthorId())) {
                    throw new IOException("For a book with ID " + b.getId() + " author with ID not found " + b.getAuthorId());
                }
            }

            rebuildIndexes(storage.loadIndexedColumns(indexesFile));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load the data.", e);
        }
    }

    private void rebuildIndexes() {
        Set<String> oldIndexes = indexes.indexedColumns();
        IndexManager rebuilt = new IndexManager();
        for (String fullName : oldIndexes) {
            String[] parts = fullName.split("\\.", 2);
            if (parts.length != 2) continue;
            String table = parts[0];
            String column = parts[1];

            try {
                if (table.equals("authors")) {
                    rebuilt.createIndex("restored_" + column, "authors", column, authors.keySet(), id -> authorValue(authors.get(id), column));
                } else if (table.equals("books")) {
                    rebuilt.createIndex("restored_" + column, "books", column, books.keySet(), id -> bookValue(books.get(id), column));
                }
            } catch (RuntimeException e) {
                System.err.println("Failed to restore the index: " + fullName);
            }
        }

        indexes = rebuilt;
        saveAll();
    }
    /**
     * @return an unmodifiable view of the authors table
     */
    public Map<Integer, Author> getAuthors() {
        return Collections.unmodifiableMap(authors);
    }
    /**
     * @return an unmodifiable view of the table of books
     */
    public Map<Integer, Book> getBooks() {
        return Collections.unmodifiableMap(books);
    }
    /**
     * @return a manager for executing indexed queries
     */
    public IndexManager getIndexes() {
        return indexes;
    }
    /**
     * Adds an author, checking for the uniqueness of the primary key.
     */
    public void insertAuthor(Author author) {
        if (authors.containsKey(author.getId())) {
            throw new IllegalArgumentException("An author with this ID already exists.");
        }

        authors.put(author.getId(), author);
        updateIndexes("authors", author.getId(), null, author);
        saveAll();
    }
    /**
     * Adds a book, verifying the uniqueness of the ID and the validity of the author's foreign key.
     */
    public void insertBook(Book book) {
        if (books.containsKey(book.getId())) {
            throw new IllegalArgumentException("A book with this ID already exists.");
        }

        if (!authors.containsKey(book.getAuthorId())) {
            throw new IllegalArgumentException("An author with this ID does not exist.");
        }

        books.put(book.getId(), book);
        updateIndexes("books", book.getId(), null, book);
        saveAll();
    }
    /**
     * Saves tables and index descriptions to CSV.
     */
    public void saveAll() {
        try {
            Files.createDirectories(dataDirectory);
            authorDao.save(authorsFile, authors.values());
            bookDao.save(booksFile, books.values());
            storage.saveIndexedColumns(indexesFile, indexes.indexedColumns());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save data", e);
        }
    }

    private void rebuildIndexes(Set<String> indexedColumns) {
        indexes = new IndexManager();
        for (String entry : indexedColumns) {
            String[] parts = entry.split("\\.", 2);
            if (parts.length != 2) {
                continue;
            }

            String table = parts[0];
            String column = parts[1];
            createIndex("restored_" + table + "_" + column, table, column);
        }
    }
    /**
     * Retrieves the value of the author column by column name.
     */
    public String authorValue(Author a, String column) {
        switch (column.toLowerCase()) {
            case "id":
                return String.valueOf(a.getId());
            case "name":
                return a.getName();
            case "birthdate":
                return String.valueOf(a.getBirthDate());
            case "alive":
                return String.valueOf(a.isAlive());
            default:
                throw new IllegalArgumentException("Unknown column Authors: " + column);
        }
    }
    /**
     * Retrieves the value of a workbook column by its name.
     */
    public String bookValue(Book b, String column) {
        switch (column.toLowerCase()) {
            case "id":
                return String.valueOf(b.getId());
            case "title":
                return b.getTitle();
            case "authorid":
                return String.valueOf(b.getAuthorId());
            case "price":
                return String.valueOf(b.getPrice());
            case "publishdate":
                return String.valueOf(b.getPublishDate());
            case "available":
                return String.valueOf(b.isAvailable());
            default:
                throw new IllegalArgumentException("Unknown column Books: " + column);
        }
    }

    private void updateIndexes(String table, int id, Object oldObject, Object newObject) {
        for (String fullName : indexes.indexedColumns()) {
            String prefix = table + ".";
            if (!fullName.startsWith(prefix)) continue;
            String column = fullName.substring(prefix.length());
            if (oldObject != null) {
                String oldValue;
                if (oldObject instanceof Author a) {
                    oldValue = authorValue(a, column);
                } else {
                    oldValue = bookValue((Book) oldObject, column);
                }
                indexes.remove(table, column, oldValue, id);
            }

            if (newObject != null) {
                String newValue;
                if (newObject instanceof Author a) {
                    newValue = authorValue(a, column);
                } else {
                    newValue = bookValue((Book) newObject, column);
                }
                indexes.add(table, column, newValue, id);
            }
        }
    }
    /**
     * Updates the author, with synchronous index updates.
     */
    public void updateAuthor(Author old, Author updated) {
        authors.put(old.getId(), updated);
        updateIndexes("authors", old.getId(), old, updated);
        saveAll();
    }
    /**
     * Updates the book and checks the reference to an existing author.
     */
    public void updateBook(Book old, Book updated) {
        if (!authors.containsKey(updated.getAuthorId())) {
            throw new IllegalArgumentException("The specified author does not exist.");
        }

        books.put(old.getId(), updated);
        updateIndexes("books", old.getId(), old, updated);
        saveAll();
    }
    /**
     * Deletes an author if no books reference them.
     */
    public void deleteAuthor(int id) {
        boolean used = books.values().stream().anyMatch(b -> b.getAuthorId() == id);
        if (used) {
            throw new IllegalArgumentException("Cannot delete the author: there are books that reference them.");
        }

        Author removed = authors.remove(id);
        if (removed != null) {
            updateIndexes("authors", id, removed, null);
        }

        saveAll();
    }
    /**
     * Removes a book and updates the indices.
     */
    public void deleteBook(int id) {
        Book removed = books.remove(id);
        if (removed != null) {
            updateIndexes("books", id, removed, null);
        }

        saveAll();
    }
    /**
     * Creates an index for an existing table column.
     */
    public void createIndex(String name, String table, String column) {
        if (table.equalsIgnoreCase("authors")) {
            indexes.createIndex(name, "authors", column, authors.keySet(), id -> authorValue(authors.get(id), column));
        } else if (table.equalsIgnoreCase("books")) {
            indexes.createIndex(name, "books", column, books.keySet(), id -> bookValue(books.get(id), column));
        } else {
            throw new IllegalArgumentException("Unknown table: " + table);
        }

        saveAll();
    }
}
