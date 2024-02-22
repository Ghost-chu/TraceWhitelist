package com.ghostchu.plugins.riawhitelist.command;

import com.ghostchu.plugins.riawhitelist.RIAWhitelist;
import com.ghostchu.plugins.riawhitelist.manager.bean.WhitelistRecord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.time.Instant;
import java.util.StringJoiner;

public class RIAWlAdd extends Command {
    private final RIAWhitelist plugin;

    public RIAWlAdd(RIAWhitelist plugin) {
        super("riawladd", "riawhitelist.add", "riawhitelistadd", "rwladd");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length < 6) {
            plugin.adventure().sender(commandSender).sendMessage(plugin.text("wladd.bad-arguments"));
            return;
        }
        String player = strings[0];
        String email = strings[1];
        String contact = strings[2];
        String operator = commandSender.getName();
        String guarantor = strings[3];
        String train = strings[4];
        StringJoiner joiner = new StringJoiner(" ");
        for (int i = 5; i < strings.length; i++) {
            joiner.add(strings[i]);
        }
        String description = joiner.toString();
        if (guarantor.equalsIgnoreCase("SakamotoSan")) {
            guarantor = null;
        }
        if (train.equalsIgnoreCase("RIA-000")) {
            train = null;
        }
        plugin.getWhitelistManager().addWhitelist(new WhitelistRecord(
                0,
                Instant.now(),
                player,
                email,
                contact,
                operator,
                guarantor,
                train,
                description,
                0,
                null,
                null
        )).thenAccept(r -> {
            if (r <= 0) {
                plugin.adventure().sender(commandSender).sendMessage(plugin.text("wladd.error-no-updates", player));
                return;
            }
            plugin.adventure().sender(commandSender).sendMessage(plugin.text("wladd.success", player, r));
        }).exceptionally(err -> {
            err.printStackTrace();
            plugin.adventure().sender(commandSender).sendMessage(plugin.text("general.internal-error", err.getMessage()));
            return null;
        });
    }
}
