package com.ds.navigation.service;

import com.ds.navigation.model.Edge;
import com.ds.navigation.model.Graph;
import com.ds.navigation.model.PathResult;
import com.ds.navigation.model.Vertex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PathFinderServiceTest {
    @Test
    void shouldFindShortestDistanceAndShortestTimeSeparately() {
        Graph graph = new Graph();
        graph.addVertex(new Vertex(0, 0, 0, "A", "普通地点"));
        graph.addVertex(new Vertex(1, 1, 0, "B", "普通地点"));
        graph.addVertex(new Vertex(2, 0, 2, "C", "普通地点"));
        graph.addVertex(new Vertex(3, 1, 2, "D", "普通地点"));

        Edge e1 = new Edge(1, 0, 1, 1.0, 1, 1);
        Edge e2 = new Edge(2, 1, 3, 1.0, 1, 1);
        Edge e3 = new Edge(3, 0, 2, 2.0, 5, 0);
        Edge e4 = new Edge(4, 2, 3, 2.0, 5, 0);
        graph.addEdge(e1);
        graph.addEdge(e2);
        graph.addEdge(e3);
        graph.addEdge(e4);

        PathFinderService service = new PathFinderService(0.8, 0.05);
        service.setGraph(graph);

        PathResult distanceResult = service.findShortestByDistance(0, 3);
        PathResult timeResult = service.findShortestByTime(0, 3);

        Assertions.assertEquals(2.0, distanceResult.getTotalDistance(), 0.0001);
        Assertions.assertEquals(4.0, timeResult.getTotalDistance(), 0.0001);
        Assertions.assertNotEquals(distanceResult.getEdgeIds(), timeResult.getEdgeIds());
    }
}
