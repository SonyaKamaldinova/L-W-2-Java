package library.tables;

import library.Database;
import library.classes.Book;

import java.util.Collection;

public class BooksTable {
    private final Database database;
    public BooksTable(Database database) {
        this.database = database;
    }

    public Collection<Book> findAll() {
        return database.getBooks().values();
    }

    public Book findById(int id) {
        return database.getBooks().get(id);
    }

    public void insert(Book book) {
        database.insertBook(book);
    }

    public void delete(int id) {
        database.deleteBook(id);
    }
}