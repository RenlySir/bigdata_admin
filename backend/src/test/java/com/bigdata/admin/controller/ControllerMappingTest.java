package com.bigdata.admin.controller;

import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class ControllerMappingTest {

    @Test
    void etlController_ShouldNotDuplicateGlobalApiPrefix() {
        RequestMapping mapping = EtlController.class.getAnnotation(RequestMapping.class);

        assertArrayEquals(new String[]{"/etl"}, mapping.value());
    }

    @Test
    void monitoringController_ShouldNotDuplicateGlobalApiPrefix() {
        RequestMapping mapping = MonitoringController.class.getAnnotation(RequestMapping.class);

        assertArrayEquals(new String[]{"/monitoring"}, mapping.value());
    }

    @Test
    void dataSourceConnectionController_ShouldNotDuplicateGlobalApiPrefix() {
        RequestMapping mapping = DataSourceConnectionController.class.getAnnotation(RequestMapping.class);

        assertArrayEquals(new String[]{"/datasources/connections"}, mapping.value());
    }
}
