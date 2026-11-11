package library;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;

public class IndexManager {
    private final Map<String, Map<String, Set<Integer>>> indexes = new HashMap<>();
    private final Map<String, String> indexNames = new HashMap<>();

    private String key(String table, String column) {
        return table.toLowerCase() + "." + column.toLowerCase();
    }

    public void createIndex(String name, String table, String column, Collection<Integer> ids, java.util.function.IntFunction<String> getter) {
        String key = key(table, column);
        if (indexes.containsKey(key)) {
            throw new IllegalArgumentException("Индекс для этого столбца уже существует.");
        }

        if (indexNames.containsValue(name)) {
            throw new IllegalArgumentException("Имя индекса уже используется.");
        }

        Map<String, Set<Integer>> index = new HashMap<>();

        for (int id : ids) {
            String value = getter.apply(id);
            if (value != null) {
                index.putIfAbsent(value, new HashSet<>());
                index.get(value).add(id);
            }
        }

        indexes.put(key, index);
        indexNames.put(key, name);
    }

    public boolean hasIndex(String table, String column) {
        return indexes.containsKey(key(table, column));
    }

    public Set<Integer> find(String table, String column, String value) {
        Map<String, Set<Integer>> index = indexes.get(key(table, column));

        if (index == null) {
            return null;
        }

        return new HashSet<>(index.getOrDefault(value, Collections.emptySet()));
    }

    public void add(String table, String column, String value, int id) {
        Map<String, Set<Integer>> index = indexes.get(key(table, column));

        if (index != null) {
            index.putIfAbsent(value, new HashSet<>());
            index.get(value).add(id);
        }
    }

    public void remove(String table, String column, String value, int id) {
        Map<String, Set<Integer>> index = indexes.get(key(table, column));

        if (index == null) return;
        Set<Integer> ids = index.get(value);
        if (ids != null) {
            ids.remove(id);
            if (ids.isEmpty()) {
                index.remove(value);
            }
        }
    }

    public Set<String> indexedColumns() {
        return new HashSet<>(indexes.keySet());
    }
}