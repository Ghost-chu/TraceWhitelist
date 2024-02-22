package com.ghostchu.plugins.riawhitelist.listener;

import com.ghostchu.plugins.riawhitelist.RIAWhitelist;
import com.ghostchu.plugins.riawhitelist.manager.bean.FastWhitelistQuery;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class WhitelistListener implements Listener {
    private final RIAWhitelist plugin;

    public WhitelistListener(RIAWhitelist plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handleWhitelist(PreLoginEvent event) {
        event.registerIntent(plugin);
        try {
            String username = event.getConnection().getName();
            if (username == null) {
                event.setCancelled(true);
                event.setCancelReason("[RIAWhitelist] Username in connection cannot be null");
                return;
            }
            try {
                FastWhitelistQuery query = plugin.getWhitelistManager().fastCheckWhitelist(username).join();
                if (!query.isWhitelisted()) {
                    event.setCancelled(true);
                    event.setCancelReason(LegacyComponentSerializer.legacySection().serialize(plugin.text("general.reject-join-no-whitelist")));
                    return;
                }
                if (!query.isAllowed()) {
                    event.setCancelled(true);
                    event.setCancelReason(LegacyComponentSerializer.legacySection().serialize(plugin.text("general.reject-join-incorrect-cases", username, query.getCorrectWhitelistedName())));
                }
            } catch (Exception e) {
                e.printStackTrace();
                event.setCancelled(true);
                event.setCancelReason(LegacyComponentSerializer.legacySection().serialize(plugin.text("general.internal-error", e.getMessage())));
            }
        } finally {
            event.completeIntent(plugin);
        }
    }
}
