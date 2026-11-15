package library.tables;

import library.Database;
import library.classes.Author;
import library.RecordIterator;

import java.util.Collection;
import java.util.Iterator;


/**
 * A table for read and modification operations on Author entities.
 * It encapsulates access to the table and provides an iterator over the records.
 */
public class AuthorsTable implements Iterable<Author> {
    private final Database db;
    /**
     * Creates a table associated with a database.
     */
    public AuthorsTable(Database database) {
        this.db = database;
    }
    /**
     * @return all records
     */
    public Collection<Author> findAll() {
        return db.getAuthors().values();
    }
    /**
     * @return the record with the specified ID, or null if it does not exist
     */
    public Author findById(int id) {
        return db.getAuthors().get(id);
    }
    /**
     * Adds a record via the database layer.
     */
    public void insert(Author author) {
        db.insertAuthor(author);
    }
    /**
     * Deletes a record by its primary key.
     */
    public void delete(int id) {
        db.deleteAuthor(id);
    }
    /**
     * @return an iterator over a snapshot of the current records
     */
    @Override
    public Iterator<Author> iterator() {
        return new RecordIterator<>(findAll());
    }
}