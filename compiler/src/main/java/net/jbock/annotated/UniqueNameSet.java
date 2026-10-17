package net.jbock.annotated;

import java.util.HashSet;
import java.util.Set;

/** A collector for names to be used in the same namespace that should not conflict. */
public final class UniqueNameSet {
    private final Set<String> uniqueNames;

    UniqueNameSet(int numElements) {
        this.uniqueNames = HashSet.newHashSet(numElements);
    }

    void reserve(String name) {
        if (uniqueNames.contains(name)) {
            throw new AssertionError("already taken: " + name);
        }
        uniqueNames.add(name);
    }

    /**
     * Generates a unique name using {@code base}. If {@code base} has not yet been added, it will be
     * returned as-is. If your {@code base} is healthy, this will always return {@code base}.
     */
    public String getUniqueName(String base) {
        String name = base;
        for (int differentiator = 2; !uniqueNames.add(name); differentiator++) {
            name = base + differentiator;
        }
        return name;
    }
}
