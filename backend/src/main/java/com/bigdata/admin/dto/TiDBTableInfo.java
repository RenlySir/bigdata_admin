package com.bigdata.admin.dto;

/**
 * TiDB Table Information
 */
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

    // Constructors
    public TiDBTableInfo() {}

    private TiDBTableInfo(Builder builder) {
        this.name = builder.name;
        this.database = builder.database;
        this.dataSourceId = builder.dataSourceId;
        this.engine = builder.engine;
        this.rowCount = builder.rowCount;
        this.dataLength = builder.dataLength;
        this.indexLength = builder.indexLength;
        this.createTime = builder.createTime;
        this.updateTime = builder.updateTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public Long getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(Long dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public Long getRowCount() {
        return rowCount;
    }

    public void setRowCount(Long rowCount) {
        this.rowCount = rowCount;
    }

    public Long getDataLength() {
        return dataLength;
    }

    public void setDataLength(Long dataLength) {
        this.dataLength = dataLength;
    }

    public Long getIndexLength() {
        return indexLength;
    }

    public void setIndexLength(Long indexLength) {
        this.indexLength = indexLength;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public static class Builder {
        private String name;
        private String database;
        private Long dataSourceId;
        private String engine;
        private Long rowCount;
        private Long dataLength;
        private Long indexLength;
        private String createTime;
        private String updateTime;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder database(String database) {
            this.database = database;
            return this;
        }

        public Builder dataSourceId(Long dataSourceId) {
            this.dataSourceId = dataSourceId;
            return this;
        }

        public Builder engine(String engine) {
            this.engine = engine;
            return this;
        }

        public Builder rowCount(Long rowCount) {
            this.rowCount = rowCount;
            return this;
        }

        public Builder dataLength(Long dataLength) {
            this.dataLength = dataLength;
            return this;
        }

        public Builder indexLength(Long indexLength) {
            this.indexLength = indexLength;
            return this;
        }

        public Builder createTime(String createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder updateTime(String updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public TiDBTableInfo build() {
            return new TiDBTableInfo(this);
        }
    }
}
