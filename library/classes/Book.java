package library.classes;

import java.time.LocalDate;

public class Book {
    private final int id;
    private String title;
    private int authorId;
    private double price;
    private LocalDate publishDate;
    private boolean available;

    public Book(int id, String title, int authorId, double price, LocalDate publishDate, boolean available) {
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        this.price = price;
        this.publishDate = publishDate;
        this.available = available;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getAuthorId() {
        return authorId;
    }

    public double getPrice() {
        return price;
    }

    public LocalDate getPublishDate() {
        return publishDate;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setPublishDate(LocalDate publishDate) {
        this.publishDate = publishDate;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return id + "  " + title + "  " + authorId + "  " + price + "  " + publishDate + "  " + available;
    }
}