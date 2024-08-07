package com.ghostchu.plugins.twhitelist.manager;

import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.plugins.twhitelist.NameMapper;
import com.ghostchu.plugins.twhitelist.TraceWhitelist;
import com.ghostchu.plugins.twhitelist.database.DataTables;
import com.ghostchu.plugins.twhitelist.database.DatabaseManager;
import com.ghostchu.plugins.twhitelist.manager.bean.FastWhitelistQuery;
import com.ghostchu.plugins.twhitelist.manager.bean.WhitelistRecord;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WhitelistManager {
    private final DatabaseManager db;
    private final TraceWhitelist plugin;

    public WhitelistManager(TraceWhitelist plugin, DatabaseManager db, NameMapper nameMapper) {
        this.plugin = plugin;
        this.db = db;
    }

    public CompletableFuture<FastWhitelistQuery> fastCheckWhitelist(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (SQLQuery query = DataTables.WHITELIST.createQuery()
                    .addCondition("player", uuid)
                    .addCondition("deleteAt", 0L)
                    .setLimit(1)
                    .build().execute();
                 ResultSet set = query.getResultSet()) {
                if (set.next()) {
                    long id = set.getLong("id");
                    return new FastWhitelistQuery(true, id);
                }
                return new FastWhitelistQuery(false, -1);
            } catch (SQLException e) {
                throw new RuntimeException("在数据库中查询白名单（快速）时出现错误：" + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<Integer> addWhitelist(WhitelistRecord record) {
        return CompletableFuture.supplyAsync(() -> {
            FastWhitelistQuery query = fastCheckWhitelist(record.getPlayer()).join();
            if (query.isWhitelisted()) {
                return -1;
            }
            return DataTables.WHITELIST.createInsert()
                    .setColumnNames("time", "player", "operator", "guarantor", "train", "description", "deleteAt", "deleteReason", "deleteOperator")
                    .setParams(new Timestamp(Instant.now().toEpochMilli()), record.getPlayer(), record.getOperator(), record.getGuarantor(), record.getTrain(),
                            record.getDescription(), record.getDeleteAt(), record.getDeleteReason(), record.getDeleteOperator())
                    .returnGeneratedKey()
                    .executeFuture(i -> i)
                    .join();
        });
    }

    public CompletableFuture<Integer> removeWhitelist(UUID uuid, UUID operator, String reason) {
        return CompletableFuture.supplyAsync(() -> {
            FastWhitelistQuery whitelistQuery = fastCheckWhitelist(uuid).join();
            if (!whitelistQuery.isWhitelisted()) {
                return -1;
            }
            try {
                return DataTables.WHITELIST.createUpdate()
                        .addCondition("id", whitelistQuery.getRecordId())
                        .addColumnValue("deleteAt", System.currentTimeMillis())
                        .addColumnValue("deleteReason", reason)
                        .addColumnValue("deleteOperator", operator)
                        .build()
                        .execute();
            } catch (SQLException e) {
                throw new RuntimeException("移除白名单时出现错误：" + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<List<WhitelistRecord>> queryWhitelist(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<WhitelistRecord> records = new ArrayList<>();
            try (SQLQuery query = DataTables.WHITELIST.createQuery()
                    .addCondition("player", uuid)
                    .orderBy("time", true)
                    .build().execute();
                 ResultSet set = query.getResultSet()) {
                while (set.next()) {
                    records.add(new WhitelistRecord(
                            set.getLong("id"),
                            set.getTimestamp("time").toInstant(),
                            UUID.fromString(set.getString("player")),
                            UUID.fromString(set.getString("operator")),
                            UUID.fromString(set.getString("guarantor")),
                            set.getString("train"),
                            set.getString("description"),
                            set.getLong("deleteAt"),
                            set.getString("deleteReason"),
                            UUID.fromString(set.getString("deleteOperator"))
                    ));
                }
                return records;
            } catch (SQLException e) {
                throw new RuntimeException("在数据库中查询白名单（完整）时出现错误：" + e.getMessage(), e);
            }
        });
    }


}
