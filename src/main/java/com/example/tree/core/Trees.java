package com.example.tree.core;

import java.util.*;

public class Trees {
    public static <T> List<List<Tree.Node<T>>> levels(Tree.Node<T> root) {
        List<List<Tree.Node<T>>> levels = new ArrayList<>();
        Deque<Map.Entry<Tree.Node<T>, Integer>> stack = new ArrayDeque<>();
        stack.push(new AbstractMap.SimpleEntry<>(root, 0));
        do {
            Map.Entry<Tree.Node<T>, Integer> entry = stack.pop();
            Tree.Node<T> node = entry.getKey();
            Integer level = entry.getValue();
            List<Tree.Node<T>> nodes;
            if (level < levels.size()) {
                nodes = levels.get(level);
            } else {
                nodes = new ArrayList<>();
                levels.add(nodes);
            }
            nodes.add(node);
            level++;
            if (node.getRight() != null) {
                stack.push(new AbstractMap.SimpleEntry<>(node.getRight(), level));
            }
            if (node.getLeft() != null) {
                stack.push(new AbstractMap.SimpleEntry<>(node.getLeft(), level));
            }
        } while (!stack.isEmpty());
        return levels;
    }
}

