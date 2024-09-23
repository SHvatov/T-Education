package ru.tbank.edu.fintech.lecture8.java.util.concurrent.executor;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;


public class ExecutorTreeTest {

    @Setter
    @Getter
    @Accessors(chain = true)
    @RequiredArgsConstructor
    private static class BinaryNode<V> {

        private final V value;

        private BinaryNode<V> left;
        private BinaryNode<V> right;

        public boolean isEmpty() {
            return value == null;
        }

    }

    @Test
    @SneakyThrows
    @DisplayName("Параллельный поиск данных в дереве")
    void test() {
        var values = new ArrayList<>(IntStream.range(0, 10).boxed().toList());
        var tree = prepareTree(values);
        printTree(tree);

        try (var service = Executors.newFixedThreadPool(4)) {
            // TODO
        }
    }

    private static BinaryNode<Integer> prepareTree(List<Integer> values) {
        if (values.isEmpty()) {
            return null;
        }

        var root = new BinaryNode<Integer>(values.removeFirst());

        var nodes = new ArrayDeque<BinaryNode<Integer>>();
        nodes.add(root);

        while (!nodes.isEmpty()) {
            var temp = new ArrayDeque<BinaryNode<Integer>>();
            while (!nodes.isEmpty()) {
                var node = nodes.removeFirst();

                var left = new BinaryNode<>(removeFirstOrNull(values));
                if (left.isEmpty()) {
                    return root;
                }
                node.setLeft(left);
                temp.add(left);

                var right = new BinaryNode<>(removeFirstOrNull(values));
                if (right.isEmpty()) {
                    return root;
                }
                node.setRight(right);
                temp.add(right);
            }
            nodes.addAll(temp);
        }

        return root;
    }

    private static void printTree(BinaryNode<Integer> root) {
        if (root == null) {
            System.out.println("Дерево пустое!");
            return;
        }

        var nodes = new ArrayDeque<BinaryNode<Integer>>();
        nodes.add(root);

        var depth = 1;
        while (!nodes.isEmpty()) {
            var temp = new ArrayDeque<BinaryNode<Integer>>();
            while (!nodes.isEmpty()) {
                var node = nodes.removeFirst();
                if (node.isEmpty()) {
                    return;
                }

                if (node.getLeft() != null) {
                    temp.add(node.getLeft());
                }

                if (node.getRight() != null) {
                    temp.add(node.getRight());
                }
            }

            nodes.addAll(temp);
            depth++;
        }

        nodes = new ArrayDeque<>();
        nodes.add(root);

        while (!nodes.isEmpty()) {
            var temp = new ArrayDeque<BinaryNode<Integer>>();

            var leaves = new StringBuilder();
            for (int i = 0; i < depth; i++) {
                leaves.insert(0, " ");
            }

            while (!nodes.isEmpty()) {
                var node = nodes.removeFirst();
                if (node.isEmpty()) {
                    return;
                }

                leaves
                        .append(" ")
                        .append(node.getValue())
                        .append(" ");

                if (node.getLeft() != null) {
                    temp.add(node.getLeft());
                }

                if (node.getRight() != null) {
                    temp.add(node.getRight());
                }
            }

            System.out.println(leaves);
            nodes.addAll(temp);
            depth--;
        }
    }

    private static <V> V removeFirstOrNull(List<V> values) {
        return values.isEmpty() ? null : values.removeFirst();
    }

}
