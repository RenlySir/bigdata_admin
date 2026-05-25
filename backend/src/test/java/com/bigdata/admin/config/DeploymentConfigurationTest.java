package com.bigdata.admin.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeploymentConfigurationTest {

    @Test
    void dockerCompose_ShouldWireTiDbAndTiKvThroughPlacementDriver() throws IOException {
        String compose = Files.readString(Path.of("../docker-compose.yml"));

        assertTrue(compose.contains("--path=pd:2379"));
        assertTrue(compose.contains("--pd=pd:2379"));
        assertFalse(compose.contains("--path=tidb://tikv:20160"));
    }
}
