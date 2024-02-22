package com.ghostchu.plugins.riawhitelist.database;


import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.YamlConfiguration;

public class HikariUtil {
    private HikariUtil() {
    }

    public static cc.carm.lib.easysql.hikari.HikariConfig createHikariConfig(Configuration section) {
        cc.carm.lib.easysql.hikari.HikariConfig config = new cc.carm.lib.easysql.hikari.HikariConfig();
        if (section == null) {
            throw new IllegalArgumentException("database.properties section in configuration not found");
        }
        for (String key : section.getKeys()) {
            config.addDataSourceProperty(key, section.getString(key));
        }
        return config;
    }
}
