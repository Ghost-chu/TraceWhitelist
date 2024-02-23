package com.ghostchu.plugins.riawhitelist.database;

import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.api.action.PreparedSQLUpdateAction;
import cc.carm.lib.easysql.api.action.PreparedSQLUpdateBatchAction;
import cc.carm.lib.easysql.api.builder.*;
import cc.carm.lib.easysql.api.enums.IndexType;
import cc.carm.lib.easysql.api.function.SQLHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public enum DataTables {
    WHITELIST("whitelist", (table) -> {
        table.addAutoIncrementColumn("id", true);
        table.addColumn("time", "DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP"); // 插入时间
        table.addColumn("player","VARCHAR(64) NOT NULL"); // 玩家ID，大小写敏感，添加时检查不敏感
        table.addColumn("operator","VARCHAR(64) NOT NULL"); // 操作员ID
        table.addColumn("guarantor", "VARCHAR(64) NULL");  // 担保人ID （可空）
        table.addColumn("train","VARCHAR(64) NULL");  // 火车ID （手动添加/担保：ZTH-000）
        table.addColumn("description", "VARCHAR(255) NOT NULL"); // 白名单添加原因
        table.addColumn("deleteAt", "BIGINT NOT NULL DEFAULT 0"); // 删除时间，0代表未删除
        table.addColumn("deleteReason", "VARCHAR(255) NULL"); //删除原因
        table.addColumn("deleteOperator", "VARCHAR(255) NULL"); // 删除操作员
        table.setIndex(IndexType.UNIQUE_KEY, "idx_riawhitelist_player", "player", "deleteAt");
        table.setIndex(IndexType.INDEX, "idx_riawhitelist_contact", "contact");
    });
    private final @NotNull String name;
    private final @NotNull SQLHandler<TableCreateBuilder> tableHandler;

    private String prefix;
    private SQLManager manager;

    DataTables(@NotNull String name,
               @NotNull SQLHandler<TableCreateBuilder> tableHandler) {
        this.name = name;
        this.tableHandler = tableHandler;
    }

    public static void initializeTables(@NotNull SQLManager sqlManager,
                                        @NotNull String tablePrefix) throws SQLException {
        for (DataTables value : values()) {
            value.create(sqlManager, tablePrefix);
        }
    }

    private void create(@NotNull SQLManager sqlManager, @NotNull String tablePrefix) throws SQLException {
        if (this.manager == null) {
            this.manager = sqlManager;
        }
        this.prefix = tablePrefix;

        TableCreateBuilder tableBuilder = sqlManager.createTable(this.getName());
        String newSettings = "ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        tableBuilder.setTableSettings(newSettings);
        tableHandler.accept(tableBuilder);
        tableBuilder.build().execute();
    }

    public @NotNull String getName() {
        return this.prefix + this.name;
    }

    public @NotNull DeleteBuilder createDelete() {
        return this.createDelete(this.manager);
    }

    public @NotNull DeleteBuilder createDelete(@NotNull SQLManager sqlManager) {
        return sqlManager.createDelete(this.getName());
    }

    public @NotNull InsertBuilder<PreparedSQLUpdateAction<Integer>> createInsert() {
        return this.createInsert(this.manager);
    }

    public @NotNull InsertBuilder<PreparedSQLUpdateAction<Integer>> createInsert(@NotNull SQLManager sqlManager) {
        return sqlManager.createInsert(this.getName());
    }

    public @NotNull InsertBuilder<PreparedSQLUpdateBatchAction<Integer>> createInsertBatch() {
        return this.createInsertBatch(this.manager);
    }

    public @NotNull InsertBuilder<PreparedSQLUpdateBatchAction<Integer>> createInsertBatch(@NotNull SQLManager sqlManager) {
        return sqlManager.createInsertBatch(this.getName());
    }

    public @NotNull TableQueryBuilder createQuery() {
        return this.createQuery(this.manager);
    }

    public @NotNull TableQueryBuilder createQuery(@NotNull SQLManager sqlManager) {
        return sqlManager.createQuery().inTable(this.getName());
    }

    public @NotNull ReplaceBuilder<PreparedSQLUpdateAction<Integer>> createReplace() {
        return this.createReplace(this.manager);
    }

    public @NotNull ReplaceBuilder<PreparedSQLUpdateAction<Integer>> createReplace(@NotNull SQLManager sqlManager) {
        return sqlManager.createReplace(this.getName());
    }

    public @NotNull ReplaceBuilder<PreparedSQLUpdateBatchAction<Integer>> createReplaceBatch() {
        return this.createReplaceBatch(this.manager);
    }

    public @NotNull ReplaceBuilder<PreparedSQLUpdateBatchAction<Integer>> createReplaceBatch(@NotNull SQLManager sqlManager) {
        return sqlManager.createReplaceBatch(this.getName());
    }

    public @NotNull UpdateBuilder createUpdate() {
        return this.createUpdate(this.manager);
    }

    public @NotNull UpdateBuilder createUpdate(@NotNull SQLManager sqlManager) {
        return sqlManager.createUpdate(this.getName());
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public boolean isExists() {
        return isExists(this.manager, this.prefix);
    }

    public boolean isExists(@NotNull SQLManager manager, @Nullable String prefix) {
        if (prefix != null) {
            this.prefix = prefix;
        }
        boolean match = false;
        try {
            try (Connection connection = manager.getConnection(); ResultSet rs = connection.getMetaData().getTables(null, null, "%", null)) {
                while (rs.next()) {
                    if (getName().equalsIgnoreCase(rs.getString("TABLE_NAME"))) {
                        match = true;
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return match;
    }

    public boolean purgeTable() {
        return purgeTable(this.manager);
    }

    public boolean purgeTable(@NotNull SQLManager sqlManager) {
        try {
            sqlManager.createDelete(this.getName())
                    .addCondition("1=1")
                    .build().execute();
            return true;
        } catch (SQLException e) {

            return false;
        }
    }
}
