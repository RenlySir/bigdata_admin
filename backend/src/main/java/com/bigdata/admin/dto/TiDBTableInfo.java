package com.bigdata.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TiDB Table Information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TiDBTableInfo {

    /**
     * Table name
     */
    private String name;

    /**
     * Database name
     */
    private String database;

    /**
     * Data source ID
     */
    private Long dataSourceId;

    /**
     * Table engine
     */
    private String engine;

    /**
     * Row count
     */
    private Long rowCount;

    /**
     * Data size in bytes
     */
    private Long dataLength;

    /**
     * Index size in bytes
     */
    private Long indexLength;

    /**
     * Create time
     */
    private String createTime;

    /**
     * Update time
     */
    private String updateTime;
}
