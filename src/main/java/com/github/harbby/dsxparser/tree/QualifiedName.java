package com.github.harbby.dsxparser.tree;

import com.github.harbby.gadtry.base.Iterators;

import java.util.*;

import static com.github.harbby.gadtry.base.Iterators.isEmpty;
import static com.github.harbby.gadtry.base.MoreObjects.checkArgument;
import static java.util.Locale.ENGLISH;
import static java.util.Objects.requireNonNull;

public class QualifiedName {
    private final List<String> parts;
    private final List<String> originalParts;

    public static QualifiedName of(String first, String... rest) {
        requireNonNull(first, "first is null");
        List<String> list = new ArrayList<>();
        list.add(first);
        Collections.addAll(list, rest);
        return of(list);
    }

    public static QualifiedName of(String name) {
        requireNonNull(name, "name is null");
        return of(List.of(name));
    }

    public static QualifiedName of(Collection<String> originalParts) {
        requireNonNull(originalParts, "originalParts is null");
        checkArgument(!isEmpty(originalParts), "originalParts is empty");
        List<String> parts = originalParts.stream().map(part -> part.toLowerCase(ENGLISH)).toList();
        return new QualifiedName(List.copyOf(originalParts), parts);
    }

    private QualifiedName(List<String> originalParts, List<String> parts) {
        this.originalParts = originalParts;
        this.parts = parts;
    }

    public List<String> getParts() {
        return parts;
    }

    public List<String> getOriginalParts() {
        return originalParts;
    }

    @Override
    public String toString() {
        return String.join(".", parts);
    }

    /**
     * For an identifier of the form "a.b.c.d", returns "a.b.c"
     * For an identifier of the form "a", returns absent
     *
     * @return QualifiedName
     */
    public Optional<QualifiedName> getPrefix() {
        if (parts.size() == 1) {
            return Optional.empty();
        }

        List<String> subList = parts.subList(0, parts.size() - 1);
        return Optional.of(new QualifiedName(subList, subList));
    }

    public boolean hasSuffix(QualifiedName suffix) {
        if (parts.size() < suffix.getParts().size()) {
            return false;
        }

        int start = parts.size() - suffix.getParts().size();

        return parts.subList(start, parts.size()).equals(suffix.getParts());
    }

    public String getSuffix() {
        return Iterators.getLast(parts);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return parts.equals(((QualifiedName) o).parts);
    }

    @Override
    public int hashCode() {
        return parts.hashCode();
    }
}
