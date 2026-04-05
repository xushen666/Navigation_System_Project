package com.ds.navigation.repository;

import com.ds.navigation.model.Graph;
import com.ds.navigation.service.MapGeneratorService;
import java.nio.file.Path;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GraphFileRepositoryTest {
    @TempDir
    Path tempDir;

    @Test
    void shouldSaveAndLoadGraph() {
        GraphFileRepository repository = new GraphFileRepository();
        MapGeneratorService generator = new MapGeneratorService(1000, 1000, 45, 70, 95, 0.2);
        Graph original = generator.generateMap(200, 123L);

        repository.saveGraph(original, tempDir);
        Graph loaded = repository.loadGraph(tempDir);

        Assertions.assertEquals(original.vertexCount(), loaded.vertexCount());
        Assertions.assertEquals(original.edgeCount(), loaded.edgeCount());
    }
}
