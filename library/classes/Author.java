package library.classes;

import java.time.LocalDate;

public class Author{
    private final int id;
    private String name;
    private LocalDate birthDate;
    private boolean alive;

    public Author(int id, String name,
                  LocalDate birthDate, boolean alive) {
        this.id = id;
        this.name = name;
        this.birthDate = birthDate;
        this.alive = alive;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + birthDate + " | " + alive;
    }
}
