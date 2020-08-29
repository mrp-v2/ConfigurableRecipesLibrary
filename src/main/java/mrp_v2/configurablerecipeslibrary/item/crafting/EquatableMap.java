package mrp_v2.configurablerecipeslibrary.item.crafting;

import javax.annotation.Nullable;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

class EquatableMap<K, V>
{
    private final BiFunction<K, K, Boolean> equalityComparator;
    private final ArrayList<K> keys;
    private final ArrayList<V> values;

    EquatableMap(BiFunction<K, K, Boolean> equalityComparator)
    {
        this.equalityComparator = equalityComparator;
        this.keys = new ArrayList<>();
        this.values = new ArrayList<>();
    }

    @Nullable V get(K key)
    {
        int index = getIndexOf(key);
        return index != -1 ? this.values.get(index) : null;
    }

    private int getIndexOf(K key)
    {
        int foundIndex = -1;
        for (int i = 0; i < this.keys.size(); i++)
        {
            K entry = this.keys.get(i);
            if (this.equalityComparator.apply(key, entry))
            {
                if (foundIndex != -1)
                {
                    throw new InvalidParameterException(
                            "There are multiple entries that match the given equality comparator!");
                }
                foundIndex = i;
            }
        }
        return foundIndex;
    }

    Set<K> keySet()
    {
        HashSet<K> set = new HashSet<>(this.keys.size());
        set.addAll(this.keys);
        return set;
    }

    @Nullable V put(K original, V replacement)
    {
        if (containsKey(original))
        {
            return this.values.set(getIndexOf(original), replacement);
        }
        this.keys.add(original);
        this.values.add(replacement);
        return null;
    }

    boolean containsKey(K key)
    {
        return getIndexOf(key) != -1;
    }
}
