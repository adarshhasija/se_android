package com.starsearth.one.domain;

import java.util.ArrayList;
import java.util.List;

public class ResponseTree {

    private Node<Response> root;

    public ResponseTree(Response rootData) {
        root = new Node<>();
        root.data = rootData;
        //root.children = new ArrayList<Node<Response>>();
        root.children = new ArrayList<>();
    }

    public void addChild(ResponseTree responseTree) {
        root.children.add(responseTree);
    }

    public void addChildren(List<ResponseTree> children) {
        root.children.addAll(children);
    }

    public ArrayList<ResponseTree> getChildren() {
        return getChildren();
    }

    public static class Node<Response> {
        private Response data;
        private Node<Response> parent;
        //private List<Node<Response>> children;
        private List<ResponseTree> children;
    }
}
