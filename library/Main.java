package library;


import library.Database;
import library.instrumentsForQuery.QueryExecutor;

import java.nio.file.Path;
import java.util.Scanner;

/**
 * Main class.
 */
public class Main {
    /**
     * Launches the console interface.
     */
    public static void main(String[] args) {
        Path dataDirectory = Path.of("data");
        Database database = new Database(dataDirectory);
        QueryExecutor executor = new QueryExecutor(database);

        System.out.println("================================");
        System.out.println("       Library                  ");
        System.out.println("================================");
        System.out.println("Enter an SQL command.");
        System.out.println("To get help, enter HELP.");
        System.out.println("To exit, enter EXIT.");

        try (Scanner scanner = new Scanner(System.in)) {
            StringBuilder buffer = new StringBuilder();
            while (true) {
                System.out.print(buffer.isEmpty() ? "library> " : "......> ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String line = scanner.nextLine().trim();
                if (buffer.isEmpty() && line.equalsIgnoreCase("EXIT")) {
                    break;
                }
                if (buffer.isEmpty() && line.equalsIgnoreCase("HELP")) {
                    printHelp();
                    continue;
                }
                if (line.isEmpty()) {
                    continue;
                }
                if (buffer.isEmpty() && !isSupportedCommandStart(line)) {
                    System.out.println("Error: unknown command. Enter HELP to view available commands.");
                    continue;
                }
                buffer.append(line).append(" ");
                if (!line.endsWith(";")) {
                    continue;
                }
                try {
                    executor.execute(buffer.toString());
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
                buffer.setLength(0);
            }
        }
        database.saveAll();
        System.out.println("Data saved.");
        System.out.println("Program execution has finished.");
    }

    private static boolean isSupportedCommandStart(String line) {
        int end = 0;
        while (end < line.length() && !Character.isWhitespace(line.charAt(end))) {
            end++;
        }

        String firstWord = line.substring(0, end).toUpperCase(java.util.Locale.ROOT);
        return firstWord.equals("INSERT") || firstWord.equals("SELECT") || firstWord.equals("UPDATE") || firstWord.equals("DELETE") || firstWord.equals("CREATE");
    }
    /**
     * Displays help for SQL commands.
     */
    private static void printHelp() {
        System.out.println("""
        Examples of SQL commands

        Adding an author:
        INSERT INTO Authors VALUES (1, 'Osip Mandelstam', '1891-01-14', false);

        Adding a book:
        INSERT INTO Books VALUES (1, 'Your face is tenderer than the tenderest…', 1, 339.63, '2023-02-09', true);

        Authors' conclusion:
        SELECT * FROM Authors;

        Displaying books:
        SELECT * FROM Books;

        Search:
        SELECT * FROM Books WHERE authorId = 1;

        Update:
        UPDATE Books SET price = 1500.0 WHERE id = 1;

        Removal:
        DELETE FROM Books WHERE id = 1;

        Creating an index:
        CREATE INDEX idx_books_author ON Books(authorId);

        Supported operators:
        =, !=, <>, >, <, >=, <=

        Date format: YYYY-MM-DD
        Strings are enclosed in single quotes.
        """);
    }
}