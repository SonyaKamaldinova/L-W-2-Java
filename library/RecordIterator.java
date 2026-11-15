package library;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * An iterator for sequential traversal of a snapshot of table records.
 * @param <T> record type
 */
public final class RecordIterator<T> implements Iterator<T>, Iterable<T> {
    private final List<T> snapshot;
    private int position;
    /**
     * Creates an iterator over a copy of the provided list.
     */
    public RecordIterator(Iterable<T> source) {
        this.snapshot = new java.util.ArrayList<>();
        source.forEach(snapshot::add);
    }
    /**
     * Checks for the existence of the next record.
     */
    @Override
    public boolean hasNext() {
        return position < snapshot.size();
    }
    /**
     * Returns the next record.
     */
    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException("There are no more entries.");
        }
        return snapshot.get(position++);
    }
    /**
     * Allows an iterator to be used in a for-each loop.
     */
    @Override
    public Iterator<T> iterator() {
        return this;
    }
}
