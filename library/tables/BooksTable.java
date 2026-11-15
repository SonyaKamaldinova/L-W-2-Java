package library.tables;

import library.Database;
import library.classes.Book;
import library.RecordIterator;

import java.util.Collection;
import java.util.Iterator;
/**
 * A table for reading and modifying Book entities.
 * It encapsulates access to the table and provides an iterator for the records.
 */
public final class BooksTable implements Iterable<Book>{
    private final Database db;
    /**
     * Creates a table associated with a database.
     */
    public BooksTable(Database database) {
        this.db = database;
    }
    /**
     * @return an independent list of all records
     */
    public Collection<Book> findAll() {
        return db.getBooks().values();
    }
    /**
     * @return the record with the specified ID, or null if it does not exist
     */
    public Book findById(int id) {
        return db.getBooks().get(id);
    }
    /**
     * Adds a record via the database layer.
     */
    public void insert(Book book) {
        db.insertBook(book);
    }
    /**
     * Deletes a record by its primary key.
     */
    public void delete(int id) {
        db.deleteBook(id);
    }
    /**
     * @return an iterator over a snapshot of the current records
     */
    @Override
    public Iterator<Book> iterator() {
        return new RecordIterator<>(findAll());
    }
}