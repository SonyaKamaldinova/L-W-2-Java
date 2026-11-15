package library.dao;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Abstraction of access to a single database table.
 * @param <T> type of stored entity
 */
public interface TableDao<T> {
    /**
     * Loads all records from the table.
     */
    List<T> load(Path file) throws IOException;

    /**
     * Preserves all table records.
     */
    void save(Path file, Iterable<T> records) throws IOException;
}