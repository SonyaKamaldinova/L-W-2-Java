package library.classes;

import java.time.LocalDate;

/**
 * A library book, linked to an author via authorId.
 */
public class Book {
    private final int id;
    private String title;
    private int authorId;
    private double price;
    private LocalDate publishDate;
    private boolean available;
    /**
     * Creates a book.
     * @param id          unique identifier of the book
     * @param title       title
     * @param authorId    author identifier
     * @param price       price
     * @param publishDate publication date
     * @param available   book availability
     */
    public Book(int id, String title, int authorId, double price, LocalDate publishDate, boolean available) {
        if (id < 1) throw new IllegalArgumentException("The book ID must be greater than zero.");
        this.id = id;
        this.title = title;
        this.authorId = authorId;
        this.price = price;
        this.publishDate = publishDate;
        this.available = available;
    }
    /**
     * @return book identifier
     */
    public int getId() {
        return id;
    }
    /**
     * @return the title of the book
     */
    public String getTitle() {
        return title;
    }
    /**
     * @return author identifier
     */
    public int getAuthorId() {
        return authorId;
    }
    /**
     * @return the price of the book
     */
    public double getPrice() {
        return price;
    }
    /**
     * @return publication date
     */
    public LocalDate getPublishDate() {
        return publishDate;
    }
    /**
     * @return book availability
     */
    public boolean isAvailable() {
        return available;
    }
    /**
     * Changes the title of the book.
     */
    public void setTitle(String title) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("The book title cannot be empty.");
        this.title = title;
    }
    /**
     * Changes the link to the author.
     */
    public void setAuthorId(int authorId) {
        if (authorId < 1) throw new IllegalArgumentException("The author ID must be greater than zero.");
        this.authorId = authorId;
    }
    /**
     * Changes the price.
     */
    public void setPrice(double price) {
        if (!Double.isFinite(price) || price < 0) throw new IllegalArgumentException("The price must be a non-negative number.");
        this.price = price;
    }
    /**
     * Changes the publication date.
     */
    public void setPublishDate(LocalDate publishDate) {
        if (publishDate.getYear() < 1) throw new IllegalArgumentException("The year of publish must be greater than zero.");
        if (publishDate == null) throw new IllegalArgumentException("Date of publish cannot be empty");
        this.publishDate = publishDate;
    }
    /**
     * Changes availability.
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }
    /**
     * @return a text representation of the book
     */
    @Override
    public String toString() {
        return id + "  " + title + "  " + authorId + "  " + price + "  " + publishDate + "  " + available;
    }
}