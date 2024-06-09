package com.ghostchu.plugins.twhitelist.listener;

import com.ghostchu.plugins.twhitelist.NameMapper;
import com.ghostchu.plugins.twhitelist.TraceWhitelist;
import com.ghostchu.plugins.twhitelist.manager.bean.FastWhitelistQuery;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class WhitelistListener implements Listener {
    private final TraceWhitelist plugin;
    private final NameMapper nameMapper;

    public WhitelistListener(TraceWhitelist plugin, NameMapper nameMapper) {
        this.plugin = plugin;
        this.nameMapper = nameMapper;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void writeUsernameCache(PostLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        String name = event.getPlayer().getName();
        nameMapper.update(uuid,name);
    }
    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleWhitelist(LoginEvent event) {
        UUID uuid = event.getConnection().getUniqueId();
        if (uuid == null) {
            event.setCancelled(true);
            event.setCancelReason("[TraceWhitelist] UUID in connection cannot be null");
            return;
        }
        event.registerIntent(plugin);
        CompletableFuture.runAsync(() -> {
            try {
                FastWhitelistQuery query = plugin.getWhitelistManager().fastCheckWhitelist(uuid).join();
                if (!query.isWhitelisted()) {
                    event.setCancelled(true);
                    event.setCancelReason(LegacyComponentSerializer.legacySection().serialize(plugin.text("general.reject-join-no-whitelist")));
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                event.setCancelled(true);
                event.setCancelReason(LegacyComponentSerializer.legacySection().serialize(plugin.text("general.internal-error", e.getMessage())));
            } finally {
                event.completeIntent(plugin);
            }
        });
    }
}
