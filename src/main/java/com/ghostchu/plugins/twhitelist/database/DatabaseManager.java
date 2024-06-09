package com.ghostchu.plugins.twhitelist.database;

import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.hikari.HikariConfig;
import cc.carm.lib.easysql.hikari.HikariDataSource;
import cc.carm.lib.easysql.manager.SQLManagerImpl;
import com.ghostchu.plugins.twhitelist.TraceWhitelist;
import net.md_5.bungee.config.Configuration;

public class DatabaseManager {
    private final TraceWhitelist plugin;
    private SQLManager sqlManager;
    private DatabaseDriverType databaseDriverType = null;
    private String prefix;

    public DatabaseManager(TraceWhitelist plugin) {
        this.plugin = plugin;
        init();
    }

    private void init() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Configuration databaseSection = plugin.getConfig().getSection("database");
        if (databaseSection == null) throw new IllegalArgumentException("Database section 不能为空");
        HikariConfig config = HikariUtil.createHikariConfig(databaseSection.getSection("properties"));
        try {
            this.prefix = databaseSection.getString("prefix");
            if (this.prefix == null || this.prefix.isBlank() || "none".equalsIgnoreCase(this.prefix)) {
                this.prefix = "";
            }
            if (databaseSection.getBoolean("mysql")) {
                databaseDriverType = DatabaseDriverType.MYSQL;
                this.sqlManager = connectMySQL(config, databaseSection);
            } else {
                throw new IllegalArgumentException("不支持的数据库类型");
            }
            DataTables.initializeTables(sqlManager, prefix);
        } catch (Exception e) {
            throw new IllegalStateException("无法初始化数据库连接，请检查数据库配置", e);
        }
    }

    private SQLManager connectMySQL(HikariConfig config, Configuration dbCfg) {
        databaseDriverType = DatabaseDriverType.MYSQL;
        // MySQL database - Required database be created first.
        String user = dbCfg.getString("user");
        String pass = dbCfg.getString("password");
        String host = dbCfg.getString("host");
        int port = dbCfg.getInt("port");
        String database = dbCfg.getString("database");
        boolean useSSL = dbCfg.getBoolean("usessl");
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL);
        config.setUsername(user);
        config.setPassword(pass);
        return new SQLManagerImpl(new HikariDataSource(config), "TraceWhitelist-SQLManager");
    }

    public DatabaseDriverType getDatabaseDriverType() {
        return databaseDriverType;
    }

    public SQLManager getSqlManager() {
        return sqlManager;
    }

    public String getPrefix() {
        return prefix;
    }
}
