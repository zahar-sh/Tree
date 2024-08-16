package com.example.tree.core;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class AvlTree<T> implements Tree<T> {

    private final Comparator<? super T> comparator;

    private AvlNode<T> root;

    public AvlTree(Comparator<? super T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public boolean isEmpty() {
        return root == null;
    }

    @Override
    public boolean contains(T value) {
        return find(value) != null;
    }

    @Override
    public boolean add(T value) {
        AvlNode<T> node = root;
        if (node == null) {
            root = new AvlNode<>(value);
            return true;
        }
        AvlNode<T> parent;
        int cmp;
        do {
            parent = node;
            cmp = comparator.compare(value, node.value);
            if (cmp < 0) {
                node = node.left;
            } else if (cmp > 0) {
                node = node.right;
            } else {
                return false;
            }
        } while (node != null);
        AvlNode<T> newNode = new AvlNode<>(value, parent);
        if (cmp < 0) {
            parent.left = newNode;
            parent.balance--;
        } else {
            parent.right = newNode;
            parent.balance++;
        }
        fixAfterInsertion(parent);
        return true;
    }

    @Override
    public Node<T> find(T value) {
        AvlNode<T> node = root;
        while (node != null) {
            int cmp = comparator.compare(value, node.value);
            if (cmp < 0) {
                node = node.left;
            } else if (cmp > 0) {
                node = node.right;
            } else {
                return node;
            }
        }
        return null;
    }

    @Override
    public boolean remove(T value) {
        Node<T> node = find(value);
        if (node != null) {
            delete(node);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        AvlNode<T> node = root;
        root = null;
        if (node != null) {
            while (node.left != null) {
                node = node.left;
            }
            do {
                if (node.right != null) {
                    node = node.right;
                    while (node.left != null) {
                        node = node.left;
                    }
                } else {
                    AvlNode<T> parent = node.parent;
                    while (parent != null && node == parent.right) {
                        node.value = null;
                        node.parent = null;
                        parent.left = null;
                        parent.right = null;

                        node = parent;
                        parent = parent.parent;
                    }
                    node.value = null;
                    node.parent = null;
                    if (parent != null) {
                        parent.left = null;
                    }

                    node = parent;
                }
            } while (node != null);
        }
    }

    @Override
    public Node<T> root() {
        return root;
    }

    @Override
    public Node<T> first() {
        AvlNode<T> node = root;
        if (node != null) {
            while (node.left != null) {
                node = node.left;
            }
        }
        return node;
    }

    @Override
    public Node<T> last() {
        AvlNode<T> node = root;
        if (node != null) {
            while (node.right != null) {
                node = node.right;
            }
        }
        return node;
    }

    @Override
    public Node<T> next(Node<T> treeNode) {
        if (treeNode == null) {
            return null;
        }
        AvlNode<T> node = (AvlNode<T>) treeNode;
        if (node.right != null) {
            node = node.right;
            while (node.left != null) {
                node = node.left;
            }
            return node;
        }
        AvlNode<T> parent = node.parent;
        while (parent != null && node == parent.right) {
            node = parent;
            parent = parent.parent;
        }
        return parent;
    }

    @Override
    public Node<T> prev(Node<T> treeNode) {
        if (treeNode == null) {
            return null;
        }
        AvlNode<T> node = (AvlNode<T>) treeNode;
        if (node.left != null) {
            node = node.left;
            while (node.right != null) {
                node = node.right;
            }
            return node;
        }
        AvlNode<T> parent = node.parent;
        while (parent != null && node == parent.left) {
            node = parent;
            parent = parent.left;
        }
        return parent;
    }

    @Override
    public void delete(Node<T> treeNode) {
        AvlNode<T> node = (AvlNode<T>) treeNode;
        if (node.value == null) {
            return;
        }
        if (node.left != null && node.right != null) {
            AvlNode<T> replacement = node.right;
            while (replacement.left != null) {
                replacement = replacement.left;
            }
            node.value = replacement.value;
            node = replacement;
        }
        AvlNode<T> replacement = node.left != null
                ? node.left
                : node.right;
        AvlNode<T> parent = node.parent;
        if (replacement != null) {
            replacement.parent = parent;
            boolean needFix;
            if (parent == null) {
                root = replacement;
                needFix = false;
            } else if (node == parent.left) {
                parent.left = replacement;
                parent.balance++;
                needFix = parent.balance != 1;
            } else {
                parent.right = replacement;
                parent.balance--;
                needFix = parent.balance != -1;
            }
            node.value = null;
            node.parent = null;
            node.left = null;
            node.right = null;
            if (needFix) {
                fixAfterDeletion(parent);
            }
        } else if (parent == null) {
            root = null;
        } else {
            node.value = null;
            node.parent = null;
            if (node == parent.left) {
                parent.left = null;
            } else {
                parent.right = null;
            }
            fixAfterDeletion(parent);
        }
    }

    @Override
    public boolean removeIf(Predicate<? super Node<T>> filter) {
        boolean removed = false;
        Node<T> node = first();
        Node<T> next = node;
        while (node != null) {
            next = next(next);
            if (filter.test(node)) {
                removed = true;
                delete(node);
            }
            node = next;
        }
        return removed;
    }

    @Override
    public void forEach(Consumer<? super Node<T>> action) {
        for (Node<T> node = first(); node != null; node = next(node)) {
            action.accept(node);
        }
    }

    @Override
    public void forEachDescending(Consumer<? super Node<T>> action) {
        for (Node<T> node = last(); node != null; node = prev(node)) {
            action.accept(node);
        }
    }

    @Override
    public boolean removeByValueIf(Predicate<? super T> filter) {
        return removeIf(node -> {
            return filter.test(node.getValue());
        });
    }

    @Override
    public void forEachValue(Consumer<? super T> action) {
        forEach(node -> {
            action.accept(node.getValue());
        });
    }

    @Override
    public void forEachValueDescending(Consumer<? super T> action) {
        forEachDescending(node -> {
            action.accept(node.getValue());
        });
    }

    private void rotateLeft(AvlNode<T> p) {
        if (p != null) {
            AvlNode<T> r = p.right;
            p.right = r.left;
            if (r.left != null) {
                r.left.parent = p;
            }
            r.parent = p.parent;
            if (p.parent == null) {
                root = r;
            } else if (p.parent.left == p) {
                p.parent.left = r;
            } else {
                p.parent.right = r;
            }
            r.left = p;
            p.parent = r;
        }
    }

    private void rotateRight(AvlNode<T> p) {
        if (p != null) {
            AvlNode<T> l = p.left;
            p.left = l.right;
            if (l.right != null) {
                l.right.parent = p;
            }
            l.parent = p.parent;
            if (p.parent == null) {
                root = l;
            } else if (p.parent.right == p) {
                p.parent.right = l;
            } else {
                p.parent.left = l;
            }
            l.right = p;
            p.parent = l;
        }
    }

    private void fixAfterInsertion(AvlNode<T> x) {
        while (x.balance != 0) {
            if (x.balance == 2) { // right heavy by 2?
                if (x.right.balance == 1) {
                    x.balance = 0;
                    x.right.balance = 0;
                    rotateLeft(x);
                } else { // x.right.balance = -1
                    int rlBalance = x.right.left.balance;
                    x.right.left.balance = 0;
                    x.right.balance = 0;
                    x.balance = 0;
                    if (rlBalance == 1)
                        x.balance = -1;
                    else if (rlBalance == -1)
                        x.right.balance = 1;

                    rotateRight(x.right);
                    rotateLeft(x);
                }
                break;
            } else if (x.balance == -2) {
                if (x.left.balance == -1) {
                    x.balance = 0;
                    x.left.balance = 0;
                    rotateRight(x);
                } else { // x.left.balance = 1
                    int lrBalance = x.left.right.balance;
                    x.left.right.balance = 0;
                    x.left.balance = 0;
                    x.balance = 0;
                    if (lrBalance == 1)
                        x.left.balance = -1;
                    else if (lrBalance == -1)
                        x.balance = 1;

                    rotateLeft(x.left);
                    rotateRight(x);
                }
                break;
            }

            if (x.parent == null)
                break;
            if (x.parent.left == x)
                x.parent.balance--;
            else
                x.parent.balance++;

            x = x.parent;
        }
    }

    private void fixAfterDeletion(AvlNode<T> x) {
        while (true) {
            if (x.balance == 2) { // right heavy by 2?
                if (x.right.balance == 1) {
                    x.balance = 0;
                    x.right.balance = 0;
                    rotateLeft(x);
                } else if (x.right.balance == 0) {
                    x.balance = 1;
                    x.right.balance = -1;
                    rotateLeft(x);
                    break;
                } else { // x.right.balance = -1
                    int rlBalance = x.right.left.balance;
                    x.right.left.balance = 0;
                    x.right.balance = 0;
                    x.balance = 0;
                    if (rlBalance == 1)
                        x.balance = -1;
                    else if (rlBalance == -1)
                        x.right.balance = 1;
                    rotateRight(x.right);
                    rotateLeft(x);
                }
                x = x.parent;
            } else if (x.balance == -2) {
                if (x.left.balance == -1) {
                    x.balance = 0;
                    x.left.balance = 0;
                    rotateRight(x);
                } else if (x.left.balance == 0) {
                    x.balance = -1;
                    x.left.balance = 1;
                    rotateRight(x);
                    break;
                } else { // (x.left.balance == 1)
                    int lrBalance = x.left.right.balance;
                    x.left.right.balance = 0;
                    x.left.balance = 0;
                    x.balance = 0;
                    if (lrBalance == 1)
                        x.left.balance = -1;
                    else if (lrBalance == -1)
                        x.balance = 1;
                    rotateLeft(x.left);
                    rotateRight(x);
                }
                x = x.parent;
            }

            if (x.parent == null)
                break;
            if (x.parent.left == x) {
                x.parent.balance++;
                if (x.parent.balance == 1) {
                    break;
                }
            } else {
                x.parent.balance--;
                if (x.parent.balance == -1) {
                    break;
                }
            }

            x = x.parent;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        @SuppressWarnings("unchecked")
        AvlTree<T> tree = (AvlTree<T>) o;
        Node<T> n1 = first();
        Node<T> n2 = tree.first();
        while (n1 != null) {
            if (n2 == null) {
                return false;
            }
            if (!Objects.equals(n1.getValue(), n2.getValue())) {
                return false;
            }
            n1 = next(n1);
            n2 = tree.next(n2);
        }
        return n2 == null;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (Node<T> node = first(); node != null; node = next(node)) {
            result = 31 * result + node.hashCode();
        }
        return result;
    }

    @Override
    public String toString() {
        if (isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        forEach(node -> {
            sb.append(node).append(',').append(' ');
        });
        sb.setLength(sb.length() - 2);
        sb.append(']');
        return sb.toString();
    }

    private static class AvlNode<T> implements Node<T> {

        private T value;

        private AvlNode<T> parent;

        private AvlNode<T> left;

        private AvlNode<T> right;

        private byte balance;

        private AvlNode(T value) {
            this.value = value;
        }

        private AvlNode(T value, AvlNode<T> parent) {
            this.value = value;
            this.parent = parent;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public Node<T> getParent() {
            return parent;
        }

        @Override
        public Node<T> getLeft() {
            return left;
        }

        @Override
        public Node<T> getRight() {
            return right;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AvlNode<?> node = (AvlNode<?>) o;
            return Objects.equals(value, node.value);
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('{');
            sb.append(value);
            if (parent != null) {
                sb.append(",parent");
            }
            if (left != null) {
                sb.append(",left");
            }
            if (right != null) {
                sb.append(",right");
            }
            sb.append('}');
            return sb.toString();
        }
    }
}
