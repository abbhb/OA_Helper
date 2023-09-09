package com.qc.printers.common.common.abstracts;

public abstract class TreeNode<T, D> {
    protected D id;

    protected T data;
    protected TreeNode<T, D> left;
    protected TreeNode<T, D> right;

    public TreeNode(D id, T data) {
        this.id = id;
        this.data = data;
        this.left = null;
        this.right = null;
    }

    public abstract D getParentId();

    public abstract void setLeftChild(TreeNode<T, D> child);

    public abstract void setRightChild(TreeNode<T, D> child);

    public abstract void addChild(TreeNode<T, D> child);

    public abstract TreeNode<T, D> createNode(D id, T data);
}
