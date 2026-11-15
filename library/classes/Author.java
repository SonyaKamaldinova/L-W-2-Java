package library.classes;

import java.time.LocalDate;

/**
 * The book's author in the library database.
 */
public class Author {
    private final int id;
    private String name;
    private LocalDate birthDate;
    private boolean alive;
    /**
     * Creates an author.
     * @param id        unique identifier
     * @param name      author's name
     * @param birthDate date of birth
     * @param alive     flag indicating whether the author is alive
     */
    public Author(int id, String name, LocalDate birthDate, boolean alive) {
        if (id < 1) throw new IllegalArgumentException("The author ID must be greater than zero.");
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.alive = alive;
    }
    /**
     * @return author identifier
     */
    public int getId() {
        return id;
    }
    /**
     * @return author's name
     */
    public String getName() {
        return name;
    }
    /**
     * @return date of birth
     */
    public LocalDate getBirthDate() {
        return birthDate;
    }
    /**
     * @return true if the author is alive
     */
    public boolean isAlive() {
        return alive;
    }
    /**
     * Changes the author's name.
     */
    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("The author's name cannot be empty.");
        this.name = name;
    }
    /**
     * Changes the date of birth.
     */
    public void setBirthDate(LocalDate birthDate) {
        if (birthDate.getYear() < 1) throw new IllegalArgumentException("The year of birth must be greater than zero.");
        if (birthDate == null) throw new IllegalArgumentException("Date of birth cannot be empty");
        this.birthDate = birthDate;
    }
    /**
     * Changes the author's living status.
     */
    public void setAlive(boolean alive) {
        this.alive = alive;
    }
    /**
     * @return a text representation of the author
     */
    @Override
    public String toString() {
        return id + "  " + name + "  " + birthDate + "  " + alive;
    }
}
