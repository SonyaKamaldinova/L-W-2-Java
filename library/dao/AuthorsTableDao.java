package library.dao;

import library.classes.Author;
import library.SaveManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * DAO for reading and writing authors to/from a CSV file.
 */
public final class AuthorsTableDao implements TableDao<Author> {
    private final SaveManager storage;
    /**
     * Создаёт DAO с указанным CSV-сериализатором.
     */
    public AuthorsTableDao(SaveManager storage) {
        this.storage = storage;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public List<Author> load(Path file) throws IOException {
        return storage.loadAuthors(file);
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public void save(Path file, Iterable<Author> records) throws IOException {
        storage.saveAuthors(file, records);
    }
}
