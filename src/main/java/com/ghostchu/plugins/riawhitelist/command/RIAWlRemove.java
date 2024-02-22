package com.ghostchu.plugins.riawhitelist.command;

import com.ghostchu.plugins.riawhitelist.RIAWhitelist;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.util.StringJoiner;

public class RIAWlRemove extends Command {
    private final RIAWhitelist plugin;

    public RIAWlRemove(RIAWhitelist plugin) {
        super("riawlremove", "riawhitelist.remove", "riawhitelistremove", "rwlremove");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length < 2) {
            plugin.adventure().sender(commandSender).sendMessage(plugin.text("wlremove.bad-arguments"));
            return;
        }
        String player = strings[0];
        String operator = commandSender.getName();
        StringJoiner joiner = new StringJoiner(" ");
        for (int i = 1; i < strings.length; i++) {
            joiner.add(strings[i]);
        }
        String reason = joiner.toString();
        plugin.getWhitelistManager().removeWhitelist(player, operator, reason).thenAccept(r -> {
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
