package zkstrata.utils;

public class BinaryTree<T> {
    private Node<T> root;

    public BinaryTree(Node<T> root) {
        this.root = root;
    }

    public Node<T> getRoot() {
        return root;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        return getRoot().equals(((BinaryTree) obj).getRoot());
    }

    public static class Node<T> {
        private Node<T> left;
        private Node<T> right;
        private T value;

        public Node(Node<T> left, Node<T> right) {
            this.left = left;
            this.right = right;
            this.value = null;
        }

        public Node(T value) {
            this.left = null;
            this.right = null;
            this.value = value;
        }

        public Node<T> getLeft() {
            return left;
        }

        public Node<T> getRight() {
            return right;
        }

        public T getValue() {
            return value;
        }

        public boolean isLeaf() {
            return this.left == null && this.right == null;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;

            if (getClass() != obj.getClass())
                return false;

            if (isLeaf() && !((Node) obj).isLeaf()
                    || !isLeaf() && ((Node) obj).isLeaf())
                return false;

            if (isLeaf())
                return getValue().equals(((Node) obj).getValue());
            else
                return getLeft().equals(((Node) obj).getLeft()) && getRight().equals(((Node) obj).getRight());
        }
    }
}
