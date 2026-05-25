package com.bigdata.admin.dto;

/**
 * TiDB Database Information
 */
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

    // Constructors
    public TiDBDatabaseInfo() {}

    private TiDBDatabaseInfo(Builder builder) {
        this.name = builder.name;
        this.dataSourceId = builder.dataSourceId;
        this.characterSet = builder.characterSet;
        this.collation = builder.collation;
        this.tableCount = builder.tableCount;
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

    public Long getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(Long dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getCharacterSet() {
        return characterSet;
    }

    public void setCharacterSet(String characterSet) {
        this.characterSet = characterSet;
    }

    public String getCollation() {
        return collation;
    }

    public void setCollation(String collation) {
        this.collation = collation;
    }

    public Integer getTableCount() {
        return tableCount;
    }

    public void setTableCount(Integer tableCount) {
        this.tableCount = tableCount;
    }

    public static class Builder {
        private String name;
        private Long dataSourceId;
        private String characterSet;
        private String collation;
        private Integer tableCount;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder dataSourceId(Long dataSourceId) {
            this.dataSourceId = dataSourceId;
            return this;
        }

        public Builder characterSet(String characterSet) {
            this.characterSet = characterSet;
            return this;
        }

        public Builder collation(String collation) {
            this.collation = collation;
            return this;
        }

        public Builder tableCount(Integer tableCount) {
            this.tableCount = tableCount;
            return this;
        }

        public TiDBDatabaseInfo build() {
            return new TiDBDatabaseInfo(this);
        }
    }
}
