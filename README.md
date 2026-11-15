# LibraryDB — мини-СУБД на Java

## Запуск

- java -version

- cd путь/к/папке_с_проектом

- javac -d out library/Main.java 

- java -cp out library.Main


При запуске из корня проекта данные размещаются в `data/`.

## Архитектура

- `classes` - сущности `Author` и `Book`.
- `dao` - DAO-слой: `TableDao`, `AuthorsTableDao`, `BooksTableDao`.
- `SaveManager` - чтение и запись CSV.
- `tables` — таблицы авторов и книг, предоставляющие коллекции и итераторы.
- `RecordIterator<T>` - собственный итератор по снимку записей.
- `IndexManager` — хеш-индексы.
- `instrumentsForQuery` — разбор и выполнение SQL-подобных команд.
- `Main` - главный класс.
- `Database` — координация таблиц, проверка данных и сохранение.

## Схема данных

`Authors(id PK, name, birthDate, alive)`
`Books(id PK, title, authorId FK -> Authors.id, price, publishDate, available)`

## Хранение

- `data/authors.csv` — авторы.
- `data/books.csv` — книги.
- `data/indexes.csv` — список индексированных столбцов. При запуске сами хеш-индексы строятся заново по таблицам.

CSV-файлы UTF-8, строки с запятыми и кавычками заключаются в двойные кавычки. Не редактируйте файлы во время работы
программы.

## Поддерживаемые команды

```sql
INSERT INTO Authors VALUES (1, 'Лев Толстой', '1828-09-09', false);
INSERT INTO Books VALUES (1, 'Война и мир', 1, 1200.50, '1869-01-01', true);
SELECT * FROM Authors;
SELECT * FROM Books WHERE authorId = 1;
UPDATE Books SET price = 1500.0 WHERE id = 1;
DELETE FROM Books WHERE id = 1;
CREATE INDEX idx_books_author ON Books(authorId);
```

Поддерживается одна простая часть `WHERE` с операторами `=`, `!=`, `<>`, `>`, `<`, `>=`, `<=`. `SELECT` поддерживает
`*`; JOIN, AND/OR и агрегатные функции не реализованы.

## UML — основные зависимости

```mermaid
classDiagram
    class Author{
      -int id ~readOnly~
      -String name
      -LocalDate birthDate
      -boolean alive
      +getId() int
      +getName() String 
      +getBirthDate() LocalDate 
      +isAlive() boolean
      +setName(String) void 
      +setBirthDate(LocalDate) void 
      +setAlive(boolean) void 
      +toString() String
    }
    class Book {
      -int id ~readOnly~
      -String title
      -int authorId
      -double price
      -LocalDate publishDate
      -boolean available
      +getId() int
      +getTitle() String
      +getAuthorId() int
      +getPrice() double
      +getPublishDate() LocalDate 
      +isAvailable() boolean
      +setTitle(String) void
      +setAuthorId(int) void
      +setPrice(double) void 
      +setPublishDate(LocalDate) void 
      +setAvailable(boolean) void 
      +toString() String
    }
    class TableDao~T~ {
      <<interface>>
      +load(Path) List~T~
      +save(Path, Iterable~T~) void
    }
    class AuthorsTableDao {
      -SaveManager storage ~readOnly~
      +load(Path) List~Author~
      +save(Path, Iterable~Author~) void
    }
    class BooksTableDao {
      -SaveManager storage ~readOnly~
      +load(Path) List~Book~
      +save(Path, Iterable~Book~) void
    }
    class Database {
      -SaveManager storage ~readOnly~
      -TableDao~Author~ authorDao ~readOnly~
      -TableDao~Book~ bookDao ~readOnly~
      -Path dataDirectory ~readOnly~
      -Path authorsFile ~readOnly~
      -Path booksFile ~readOnly~
      -Path indexesFile ~readOnly~
      -Map~Integer, Author~ authors
      -Map~Integer, Book~ books
      -IndexManager indexes
      +getAuthors() Map~Integer, Author~
      +getBooks() Map~Integer, Book~
      +getIndexes() IndexManager
      +insertAuthor(Author) void
      +insertBook(Book) void
      +saveAll() void 
      -rebuildIndexes(Set~String~) void
      +authorValue(Author, String) String
      +bookValue(Book, String) String
      -updateIndexes(String, int, Object, Object) void
      +updateAuthor(Author, Author) void
      +updateBook(Book, Book) void
      +deleteAuthor(int) void
      +deleteBook(int) void
      +createIndex(String, String, String) void 
    }
    class AuthorsTable {
      -Database db ~readOnly~
      +findAll() Collection~Author~
      +findById(int) Author
      +insert(Author) void
      +delete(int) void
      +iterator() Iterator~Author~
    }
    class BooksTable {
      -Database db ~readOnly~
      +findAll() Collection~Book~
      +findById(int) Book
      +insert(Book) void
      +delete(int) void
      +iterator() Iterator~Book~
    }
    class RecordIterator~T~ {
    -List<T> snapshot ~readOnly~
    -int position
    +hasNext() boolean 
    +next() T
    +iterator() Iterator<T>
    }

    TableDao <|.. AuthorsTableDao : implements
    TableDao <|.. BooksTableDao : implements
    Iterable~Author~ <|.. AuthorsTable : implements
    Iterable~Book~ <|.. BooksTable : implements
    RecordIterator <|.. Iterable~T~ : implements
    RecordIterator <|.. Iterator~T~ : implements
    Database --> TableDao
    Database --> AuthorsTableDao
    Database --> BooksTableDao
    Database --> Author
    Database --> Book
    AuthorsTable --> Database
    AuthorsTable --> Author
    BooksTable --> Database
    BooksTable --> Book
    AuthorsTable --> RecordIterator
    BooksTable --> RecordIterator
```

## UML — последовательность сохранения записи

```mermaid
sequenceDiagram
    actor U as Пользователь
    participant UI as Интерфейс
    participant Q as QueryExecutor
    participant D as Database

    U->>UI: вводит запрос
    UI->>Q: передаёт запрос
    Q->>D: выполняет запрос
    D->>D: сохраняет изменения
```

# Список источников

- https://docs.oracle.com/javase/8/docs/api/overview-summary.html
- https://www.baeldung.com/get-started-with-java-series
- https://www.geeksforgeeks.org/java/java/
- https://habr.com/ru/articles/652867/
- https://javarush.com/groups/java-developer
