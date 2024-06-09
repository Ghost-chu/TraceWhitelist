package com.ghostchu.plugins.twhitelist.command;

import com.ghostchu.plugins.twhitelist.NameMapper;
import com.ghostchu.plugins.twhitelist.TraceWhitelist;
import com.ghostchu.plugins.twhitelist.exception.ComponentMessageException;
import com.ghostchu.plugins.twhitelist.manager.bean.WhitelistRecord;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

import java.time.Instant;
import java.util.StringJoiner;
import java.util.UUID;

public class TraceWlInvite extends Command {
    private final TraceWhitelist plugin;
    private final NameMapper nameMapper;

    public TraceWlInvite(TraceWhitelist plugin, NameMapper nameMapper) {
        super("wlinvite", "twhitelist.invite","invite");
        this.plugin = plugin;
        this.nameMapper = nameMapper;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length < 3) {
            plugin.adventure().sender(commandSender).sendMessage(plugin.text("wlinvite.bad-arguments"));
            return;
        }
        if(!(commandSender instanceof ProxiedPlayer proxiedPlayer)){
            return;
        }
        plugin.adventure().sender(commandSender).sendMessage(plugin.text("general.please-wait"));
        String player = strings[0];
        String train = "PlayerInvite";
        StringJoiner joiner = new StringJoiner(" ");
        for (int i = 2; i < strings.length; i++) {
            joiner.add(strings[i]);
        }
        String description = joiner.toString();
        try {
            handle(commandSender, player, proxiedPlayer.getUniqueId(), proxiedPlayer.getUniqueId(), train, description);
        }catch (ComponentMessageException e){
            plugin.adventure().sender(commandSender).sendMessage(e.getComponentMessage());
        }
    }

    private void handle(CommandSender commandSender, String invited, UUID operator, UUID guarantor, String train, String description) throws ComponentMessageException {
        UUID invitedUUID = nameMapper.getUUID(invited).join().orElseThrow(()->new ComponentMessageException(plugin.text("general.player-not-exists", invited)));
        plugin.getWhitelistManager().addWhitelist(new WhitelistRecord(
                0,
                Instant.now(),
                invitedUUID,
                operator,
                guarantor,
                train,
                description,
                0,
                null,
                null
        )).thenAccept(r -> {
            if (r <= 0) {
                plugin.adventure().sender(commandSender).sendMessage(plugin.text("wlinvite.error-no-updates", invited));
                return;
            }
            plugin.adventure().sender(commandSender).sendMessage(plugin.text("wlinvite.success", invited, r));
        }).exceptionally(err -> {
            err.printStackTrace();
            plugin.adventure().sender(commandSender).sendMessage(plugin.text("general.internal-error", err.getMessage()));
            return null;
        });

    }

}
