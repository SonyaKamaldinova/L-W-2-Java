package library.dao;

import library.classes.Book;
import library.SaveManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * DAO for reading and writing books in CSV format.
 */
public final class BooksTableDao implements TableDao<Book> {
    private final SaveManager storage;
    /**
     * Creates a DAO with the specified CSV serializer.
     */
    public BooksTableDao(SaveManager storage) {
        this.storage = storage;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Book> load(Path file) throws IOException {
        return storage.loadBooks(file);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void save(Path file, Iterable<Book> records) throws IOException {
        storage.saveBooks(file, records);
    }
}