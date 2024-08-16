package com.example.tree.core;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Tree<T> {

    interface Node<T> {

        T getValue();

        Node<T> getParent();

        Node<T> getLeft();

        Node<T> getRight();
    }

    Comparator<? super T> comparator();

    boolean isEmpty();

    boolean contains(T value);

    boolean add(T value);

    Node<T> find(T value);

    boolean remove(T value);

    void clear();

    Node<T> root();

    Node<T> first();

    Node<T> last();

    Node<T> next(Node<T> node);

    Node<T> prev(Node<T> node);

    void delete(Node<T> node);

    boolean removeIf(Predicate<? super Node<T>> filter);

    void forEach(Consumer<? super Node<T>> action);

    void forEachDescending(Consumer<? super Node<T>> action);

    boolean removeByValueIf(Predicate<? super T> filter);

    void forEachValue(Consumer<? super T> action);

    void forEachValueDescending(Consumer<? super T> action);
}
