package library.tables;

import library.Database;
import library.classes.Book;

import java.util.Collection;

public class BooksTable {
    private final Database db;
    public BooksTable(Database database) {
        this.db = database;
    }

    public Collection<Book> findAll() {
        return db.getBooks().values();
    }

    public Book findById(int id) {
        return db.getBooks().get(id);
    }

    public void insert(Book book) {
        db.insertBook(book);
    }

    public void delete(int id) {
        db.deleteBook(id);
    }
}