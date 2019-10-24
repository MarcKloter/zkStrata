package zkstrata.domain.visitor;

import java.util.*;

/**
 * Wrapper class for {@link Map} to add methods used for semantic analysis.
 */
public class MapListener<K, V> extends AbstractMap<K, V> {
    private final Map<K, V> delegatee;
    private final Set<K> unused;

    public MapListener(Map<K, V> delegatee) {
        this.delegatee = delegatee;
        this.unused = new HashSet<>();
    }

    public Map<K,V> getUsedMap() {
        Map<K,V> map = new HashMap<>(delegatee);
        unused.forEach(map::remove);
        return map;
    }

    public Set<K> getUnusedKeySet() {
        return unused;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return delegatee.entrySet();
    }

    @Override
    public V put(K key, V value) {
        V previous = delegatee.put(key, value);

        if(previous == null)
            this.unused.add(key);

        return previous;
    }

    @Override
    public V get(Object key) {
        unused.remove(key);
        return delegatee.get(key);
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
