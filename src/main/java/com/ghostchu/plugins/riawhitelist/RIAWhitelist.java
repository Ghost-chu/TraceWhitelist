package com.ghostchu.plugins.riawhitelist;

import cc.carm.lib.easysql.EasySQL;
import com.ghostchu.plugins.riawhitelist.command.RIAWlAdd;
import com.ghostchu.plugins.riawhitelist.command.RIAWlQuery;
import com.ghostchu.plugins.riawhitelist.command.RIAWlRemove;
import com.ghostchu.plugins.riawhitelist.database.DatabaseManager;
import com.ghostchu.plugins.riawhitelist.listener.WhitelistListener;
import com.ghostchu.plugins.riawhitelist.manager.WhitelistManager;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.platform.bungeecord.BungeeAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public final class RIAWhitelist extends Plugin {
    private Configuration configuration;
    @Getter
    private DatabaseManager databaseManager;
    @Getter
    private WhitelistManager whitelistManager;

    private BungeeAudiences adventure;

    public @NonNull BungeeAudiences adventure() {
        if (this.adventure == null) {
            throw new IllegalStateException("Cannot retrieve audience provider while plugin is not enabled");
        }
        return this.adventure;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        reloadConfig();
        this.adventure = BungeeAudiences.create(this);
        setupDatabase();
        this.whitelistManager = new WhitelistManager(this, databaseManager);
        getProxy().getPluginManager().registerCommand(this, new RIAWlAdd(this));
        getProxy().getPluginManager().registerCommand(this, new RIAWlRemove(this));
        getProxy().getPluginManager().registerCommand(this, new RIAWlQuery(this));
        getProxy().getPluginManager().registerListener(this, new WhitelistListener(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        if (databaseManager != null) {
            EasySQL.shutdownManager(databaseManager.getSqlManager());
        }
        if (this.adventure != null) {
            this.adventure.close();
            this.adventure = null;
        }
    }

    private void setupDatabase() {
        try {
            this.databaseManager = new DatabaseManager(this);
        } catch (Exception e) {
            e.printStackTrace();
            getProxy().stop("[RIAWhitelist]" + e.getMessage());
        }
    }

    public void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Cannot save the configuration.", e);
        }
    }

    public void saveDefaultConfig() {
        // Create plugin config folder if it doesn't exist
        try {
            if (!getDataFolder().exists()) {
                getLogger().info("Created config folder: " + getDataFolder().mkdir());
            }
            File configFile = new File(getDataFolder(), "config.yml");
            // Copy default config if it doesn't exist
            if (!configFile.exists()) {
                FileOutputStream outputStream = new FileOutputStream(configFile); // Throws IOException
                InputStream in = getResourceAsStream("config.yml"); // This file must exist in the jar resources folder
                in.transferTo(outputStream); // Throws IOException
            }
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Cannot save the default configuration.", e);
        }
    }

    public Configuration getConfig() {
        return configuration;
    }

    public void reloadConfig() {
        try {
            this.configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Cannot reload the configuration from the file.", e);
        }
    }

    public Component text(String key, Object... args) {
        Configuration section = getConfig().getSection("lang");
        if (section == null) throw new IllegalStateException("lang section not found");
        String string = section.getString(key, "missing:" + key);
        Component component = MiniMessage.miniMessage().deserialize(string);
        return fillArgs(component, convert(args));
    }

    @NotNull
    public Component[] convert(@Nullable Object... args) {
        if (args == null || args.length == 0) {
            return new Component[0];
        }
        Component[] components = new Component[args.length];
        for (int i = 0; i < args.length; i++) {
            Object obj = args[i];
            if (obj == null) {
                components[i] = Component.empty();
                continue;
            }
            Class<?> clazz = obj.getClass();
            if (obj instanceof Component component) {
                components[i] = component;
                continue;
            }
            if (obj instanceof ComponentLike componentLike) {
                components[i] = componentLike.asComponent();
                continue;
            }
            // Check
            try {
                if (Character.class.equals(clazz)) {
                    components[i] = Component.text((char) obj);
                    continue;
                }
                if (Byte.class.equals(clazz)) {
                    components[i] = Component.text((Byte) obj);
                    continue;
                }
                if (Integer.class.equals(clazz)) {
                    components[i] = Component.text((Integer) obj);
                    continue;
                }
                if (Long.class.equals(clazz)) {
                    components[i] = Component.text((Long) obj);
                    continue;
                }
                if (Float.class.equals(clazz)) {
                    components[i] = Component.text((Float) obj);
                    continue;
                }
                if (Double.class.equals(clazz)) {
                    components[i] = Component.text((Double) obj);
                    continue;
                }
                if (Boolean.class.equals(clazz)) {
                    components[i] = Component.text((Boolean) obj);
                    continue;
                }
                if (String.class.equals(clazz)) {
                    components[i] = LegacyComponentSerializer.legacySection().deserialize((String) obj);
                    continue;
                }
                components[i] = LegacyComponentSerializer.legacySection().deserialize(obj.toString());
            } catch (Exception exception) {
                exception.printStackTrace();
                components[i] = LegacyComponentSerializer.legacySection().deserialize(obj.toString());
            }
        }
        return components;
    }

    /**
     * Replace args in origin to args
     *
     * @param origin origin
     * @param args   args
     * @return filled component
     */
    @NotNull
    public static Component fillArgs(@NotNull Component origin, @Nullable Component... args) {
        for (int i = 0; i < args.length; i++) {
            origin = origin.replaceText(TextReplacementConfig.builder()
                    .matchLiteral("{" + i + "}")
                    .replacement(args[i] == null ? Component.empty() : args[i])
                    .build());
        }
        return origin.compact();
    }


}
