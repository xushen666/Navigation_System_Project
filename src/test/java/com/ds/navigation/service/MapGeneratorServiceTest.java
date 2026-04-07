package com.ds.navigation.service;

import com.ds.navigation.model.Edge;
import com.ds.navigation.model.Graph;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MapGeneratorServiceTest {
    @Test
    void shouldGenerateConnectedLargeGraph() {
        MapGeneratorService service = new MapGeneratorService(10000, 10000, 45, 70, 95, 0.25);
        Graph graph = service.generateMap(10000, 42L);

        Assertions.assertEquals(10000, graph.vertexCount());
        Assertions.assertTrue(graph.edgeCount() >= 9999);

        Set<Integer> visited = new HashSet<>();
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(0);
        visited.add(0);
        while (!queue.isEmpty()) {
            int current = queue.poll();
            for (Edge edge : graph.getAdjacentEdges(current)) {
                int other = edge.getOther(current);
                if (visited.add(other)) {
                    queue.add(other);
                }
            }
        }
        Assertions.assertEquals(graph.vertexCount(), visited.size());
    }
}
