package smagellan.test;

import java.util.*;

public class TreeSum {
    public static void main(String[] args) {
        Node root = new Node("root", 1);
        Node l1 = new Node("l1", 2);
        Node m1  = new Node("m1", 3);
        Node r1 = new Node("r1", 4);

        Node l2 = new Node("l2", 5);
        Node m2 = new Node("m2",6);
        Node r2 = new Node("r2",7);
        Node n2_4 = new Node("n2_4",8);

        l1.setChildren(Arrays.asList(l2, m2, r2, n2_4));
        r1.setChildren(Arrays.asList(l2, m2, r2, n2_4));
        root.setChildren(Arrays.asList(l1, m1, r1));

        System.err.println(recursiveSum(root));
        System.err.println(nonRecursiveSum(root));
    }

    public static int recursiveSum(Node root) {
        List<Node> children = root.getChildren();
        int subtreeSum = 0;
        if (children != null) {
            for (Node curNode : children) {
                subtreeSum += recursiveSum(curNode);
            }
        }
        return root.getData() + subtreeSum;
    }

    public static int nonRecursiveSum(Node root) {
        Stack<Iterator<Node>> stack = new Stack<>();
        int sum = root.getData();
        System.err.println("acc:" + root.getData());
        Iterator<Node> curIter = root.childrenIterator();
        stack.push(curIter);
        while (!stack.isEmpty()) {
            while (curIter.hasNext()) {
                Node child = curIter.next();
                sum += child.getData();
                System.err.println("acc:" + child.getData());
                curIter = child.childrenIterator();
                stack.push(curIter);
            }
            Iterator<Node> top = stack.peek();
            curIter = top.hasNext() ? top : stack.pop();
        }
        return sum;
    }

    public static class Node {
        private final int data;
        private final String name;
        List<Node> children;

        public Node(String name, int data) {
            this.name = name;
            this.data = data;
            this.children = Collections.emptyList();
        }

        public int getData() {
            return data;
        }

        public List<Node> getChildren() {
            return children;
        }

        public Iterator<Node> childrenIterator() {
            return new MyIterator(children, name);
        }

        public void setChildren(List<Node> children) {
            this.children = children;
        }

        @Override
        public String toString() {
            return name + ":" + data;
        }
    }
}

class MyIterator implements Iterator<TreeSum.Node> {
    private final Iterator<TreeSum.Node> wrapped;
    private final String name;

    public MyIterator(List<TreeSum.Node> children, String name) {
        this.wrapped = children.iterator();
        this.name = name;
    }

    @Override
    public boolean hasNext() {
        return wrapped.hasNext();
    }

    @Override
    public TreeSum.Node next() {
        return wrapped.next();
    }

    @Override
    public String toString() {
        return name;
    }
}
