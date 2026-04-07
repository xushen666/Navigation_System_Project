package com.ds.navigation.service;

import com.ds.navigation.model.Graph;
import com.ds.navigation.model.Vertex;
import com.ds.navigation.util.GeometryUtils;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class SpatialIndexService {
    private Node root;

    public void buildIndex(Graph graph) {
        List<Vertex> vertices = new ArrayList<>(graph.getVertices());
        this.root = build(vertices, 0);
    }

    public Vertex findNearest(double x, double y) {
        if (root == null) {
            throw new IllegalStateException("空间索引尚未建立");
        }
        SearchResult result = nearest(root, x, y, null);
        return result.vertex();
    }

    public List<Vertex> findKNearest(double x, double y, int k) {
        if (root == null) {
            throw new IllegalStateException("空间索引尚未建立");
        }
        PriorityQueue<SearchResult> heap = new PriorityQueue<>(Comparator.comparingDouble((SearchResult r) -> r.distanceSquared()).reversed());
        kNearest(root, x, y, k, heap);
        List<Vertex> result = new ArrayList<>();
        while (!heap.isEmpty()) {
            result.add(heap.poll().vertex());
        }
        result.sort(Comparator.comparingDouble(v -> GeometryUtils.distanceSquared(v.getX(), v.getY(), x, y)));
        return result;
    }

    private Node build(List<Vertex> vertices, int depth) {
        if (vertices.isEmpty()) {
            return null;
        }
        int axis = depth % 2;
        vertices.sort(axis == 0 ? Comparator.comparingDouble(Vertex::getX) : Comparator.comparingDouble(Vertex::getY));
        int mid = vertices.size() / 2;
        Vertex median = vertices.get(mid);
        Node node = new Node(median, axis);
        node.left = build(new ArrayList<>(vertices.subList(0, mid)), depth + 1);
        node.right = build(new ArrayList<>(vertices.subList(mid + 1, vertices.size())), depth + 1);
        return node;
    }

    private SearchResult nearest(Node node, double x, double y, SearchResult best) {
        if (node == null) {
            return best;
        }
        double distanceSquared = GeometryUtils.distanceSquared(node.vertex.getX(), node.vertex.getY(), x, y);
        SearchResult current = new SearchResult(node.vertex, distanceSquared);
        if (best == null || current.distanceSquared() < best.distanceSquared()) {
            best = current;
        }

        double delta = node.axis == 0 ? x - node.vertex.getX() : y - node.vertex.getY();
        Node first = delta < 0 ? node.left : node.right;
        Node second = delta < 0 ? node.right : node.left;
        best = nearest(first, x, y, best);
        if (delta * delta < best.distanceSquared()) {
            best = nearest(second, x, y, best);
        }
        return best;
    }

    private void kNearest(Node node, double x, double y, int k, PriorityQueue<SearchResult> heap) {
        if (node == null) {
            return;
        }
        double distanceSquared = GeometryUtils.distanceSquared(node.vertex.getX(), node.vertex.getY(), x, y);
        SearchResult current = new SearchResult(node.vertex, distanceSquared);
        if (heap.size() < k) {
            heap.offer(current);
        } else if (distanceSquared < heap.peek().distanceSquared()) {
            heap.poll();
            heap.offer(current);
        }

        double delta = node.axis == 0 ? x - node.vertex.getX() : y - node.vertex.getY();
        Node first = delta < 0 ? node.left : node.right;
        Node second = delta < 0 ? node.right : node.left;
        kNearest(first, x, y, k, heap);
        if (heap.size() < k || delta * delta < heap.peek().distanceSquared()) {
            kNearest(second, x, y, k, heap);
        }
    }

    private record SearchResult(Vertex vertex, double distanceSquared) {
    }

    private static class Node {
        private final Vertex vertex;
        private final int axis;
        private Node left;
        private Node right;

        private Node(Vertex vertex, int axis) {
            this.vertex = vertex;
            this.axis = axis;
        }
    }
}
