package com.ghostchu.plugins.twhitelist.command;

import com.ghostchu.plugins.twhitelist.NameMapper;
import com.ghostchu.plugins.twhitelist.TraceWhitelist;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;

public class TraceWlRemove extends Command {
    private final TraceWhitelist plugin;
    private final NameMapper nameMapper;

    public TraceWlRemove(TraceWhitelist plugin, NameMapper nameMapper) {
        super("wlremove", "twhitelist.remove", "wlrm", "wldelete");
        this.plugin = plugin;
        this.nameMapper = nameMapper;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length < 2) {
            plugin.adventure().sender(commandSender).sendMessage(plugin.text("wlremove.bad-arguments"));
            return;
        }
        String player = strings[0];
        UUID operator = new UUID(0,0);
        if(commandSender instanceof ProxiedPlayer proxiedPlayer) {
            operator = proxiedPlayer.getUniqueId();
        }
        StringJoiner joiner = new StringJoiner(" ");
        for (int i = 1; i < strings.length; i++) {
            joiner.add(strings[i]);
        }
        String reason = joiner.toString();
        Optional<UUID> removeTarget = nameMapper.getUUID(player).join();
        if(removeTarget.isEmpty()){
            plugin.adventure().sender(commandSender).sendMessage(plugin.text("general.player-not-exists", player));
            return;
        }

        plugin.getWhitelistManager().removeWhitelist(removeTarget.get(), operator, reason).thenAccept(r -> {
            if (r <= 0) {
                plugin.adventure().sender(commandSender).sendMessage(plugin.text("wlremove.error-no-updates"));
                return;
            }
            plugin.adventure().sender(commandSender).sendMessage(plugin.text("wlremove.success", player, r));
        }).exceptionally(err -> {
            err.printStackTrace();
            plugin.adventure().sender(commandSender).sendMessage(plugin.text("general.internal-error", err.getMessage()));
            return null;
        });
    }
}
