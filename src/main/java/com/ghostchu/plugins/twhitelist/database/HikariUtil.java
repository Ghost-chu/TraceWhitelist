package com.ghostchu.plugins.twhitelist.database;


import net.md_5.bungee.config.Configuration;

public class HikariUtil {
    private HikariUtil() {
    }

    public static cc.carm.lib.easysql.hikari.HikariConfig createHikariConfig(Configuration section) {
        cc.carm.lib.easysql.hikari.HikariConfig config = new cc.carm.lib.easysql.hikari.HikariConfig();
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");
        if (section == null) {
            throw new IllegalArgumentException("database.properties section in configuration not found");
        }
        for (String key : section.getKeys()) {
            config.addDataSourceProperty(key, String.valueOf(section.get(key)));
        }
        return config;
    }
}
