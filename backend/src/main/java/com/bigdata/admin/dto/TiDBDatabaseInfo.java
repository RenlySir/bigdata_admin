package com.bigdata.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TiDB Database Information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TiDBDatabaseInfo {

    /**
     * Database name
     */
    private String name;

    /**
     * Data source ID
     */
    private Long dataSourceId;

    /**
     * Character set
     */
    private String characterSet;

    /**
     * Collation
     */
    private String collation;

    /**
     * Table count
     */
    private Integer tableCount;
}
