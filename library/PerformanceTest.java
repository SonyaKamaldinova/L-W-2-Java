package library;

import library.classes.Author;
import library.tables.AuthorsTable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class PerformanceTest {
    private static final int RECORD_COUNT = 10000;
    private static final int SEARCH_COUNT = 200;
    private static final int REPEATS = 5;
    private static final int WARMUP = 30;
    private static volatile long resultKeeper;
    public static void main(String[] args) throws Exception {
        Path dataDirectory = Path.of("performance-test-data").toAbsolutePath();
        Database database = new Database(dataDirectory);
        AuthorsTable authorsTable = new AuthorsTable(database);
        System.out.println("Creating authors...");
        for (int i = 1; i <= RECORD_COUNT; i++) {
            Author author = new Author(i, "Author " + i, LocalDate.of(1950 + i % 70, 1 + i % 12, 1 + i % 28), i % 2 == 0);
            authorsTable.insert(author);
        }

        Collection<Author> authors = authorsTable.findAll();
        System.out.println("Authors created: " + authors.size());
        database.createIndex("author_name_index", "authors", "name");
        database.createIndex("author_birthdate_index", "authors", "birthdate");
        database.createIndex("author_alive_index", "authors", "alive");
        IndexManager indexes = database.getIndexes();
        testSearch("name", authors, indexes, "Author 500", Author::getName);
        testSearch("birthDate", authors, indexes, String.valueOf(authorsTable.findById(500).getBirthDate()), author -> String.valueOf(author.getBirthDate()));
        testSearch("alive", authors, indexes, "true", author -> String.valueOf(author.isAlive()));

        System.out.println("\nPerformance tests completed.");
        System.out.println("Database directory: " + dataDirectory);
    }

    private static void testSearch(String column, Collection<Author> authors, IndexManager indexes, String searchValue, Function<Author, String> getter) {
        System.out.println("\n===== " + column + " =====");
        System.out.println("Search value: " + searchValue);
        Set<Integer> linearResult = linearSearch(authors, searchValue, getter);
        Set<Integer> indexResult = indexes.find("authors", column, searchValue);
        if (!linearResult.equals(indexResult)) {
            throw new IllegalStateException("Search results differ for column: " + column);
        }

        System.out.println("Found records: " + linearResult.size());
        long withoutIndex = measureWithoutIndex(authors, searchValue, getter);
        long withIndex = measureWithIndex(indexes, column, searchValue);
        System.out.printf("Without index: %.3f ms%n", withoutIndex / 1_000_000.0);
        System.out.printf("With index: %.3f ms%n", withIndex / 1_000_000.0);
    }

    private static Set<Integer> linearSearch(Collection<Author> authors, String value, Function<Author, String> getter) {
        Set<Integer> result = new HashSet<>();
        for (Author author : authors) {
            if (value.equals(getter.apply(author))) {
                result.add(author.getId());
            }
        }

        return result;
    }

    private static long measureWithoutIndex(Collection<Author> authors, String value, Function<Author, String> getter) {
        long total = 0;
        long checksum = 0;
        for (int repeat = 0; repeat < REPEATS; repeat++) {
            long start = System.nanoTime();
            for (int i = 0; i < SEARCH_COUNT; i++) {
                Set<Integer> result = linearSearch(authors, value, getter);
                checksum += result.size();
            }

            total += System.nanoTime() - start;
        }

        resultKeeper = checksum;
        return total / REPEATS;
    }

    private static long measureWithIndex(IndexManager indexes, String column, String value) {
        long total = 0;
        long checksum = 0;
        for (int repeat = 0; repeat < REPEATS; repeat++) {
            long start = System.nanoTime();
            for (int i = 0; i < SEARCH_COUNT; i++) {
                Set<Integer> result = indexes.find("authors", column, value);
                checksum += result.size();
            }

            total += System.nanoTime() - start;
        }

        resultKeeper = checksum;
        return total / REPEATS;
    }
}
