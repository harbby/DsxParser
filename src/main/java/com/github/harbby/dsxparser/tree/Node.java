package com.github.harbby.dsxparser.tree;

import java.util.List;
import java.util.Optional;

public abstract class Node {
    private final NodeLocation location;

    protected Node(NodeLocation location) {
        this.location = location;
    }

    public final Optional<NodeLocation> getLocation() {
        return Optional.ofNullable(location);
    }

    public abstract List<? extends Node> getChildren();

    // Force subclasses to have a proper equals and hashcode implementation
    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();
}
