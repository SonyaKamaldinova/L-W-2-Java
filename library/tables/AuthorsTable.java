package library.tables;

import library.Database;
import library.classes.Author;

import java.util.Collection;

public class AuthorsTable {
    private final Database db;

    public AuthorsTable(Database database) {
        this.db = database;
    }

    public Collection<Author> findAll() {
        return db.getAuthors().values();
    }

    public Author findById(int id) {
        return db.getAuthors().get(id);
    }

    public void insert(Author author) {
        db.insertAuthor(author);
    }

    public void delete(int id) {
        db.deleteAuthor(id);
    }
}