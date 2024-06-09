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

public class TraceWlAdd extends Command {
    private final TraceWhitelist plugin;
    private final NameMapper nameMapper;

    public TraceWlAdd(TraceWhitelist plugin, NameMapper nameMapper) {
        super("wladd", "twhitelist.add");
        this.plugin = plugin;
        this.nameMapper = nameMapper;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length < 4) {
            plugin.adventure().sender(commandSender).sendMessage(plugin.text("wladd.bad-arguments"));
            return;
        }
        plugin.adventure().sender(commandSender).sendMessage(plugin.text("general.please-wait"));
        String player = strings[0];
        String guarantor = strings[1];
        String train = strings[2];
        UUID operator = new UUID(0,0);
        StringJoiner joiner = new StringJoiner(" ");
        for (int i = 3; i < strings.length; i++) {
            joiner.add(strings[i]);
        }
        String description = joiner.toString();
        if (guarantor.equalsIgnoreCase("Console")) {
            guarantor = null;
        }
        if (train.equalsIgnoreCase("Undefined")) {
            train = null;
        }
        if(commandSender instanceof ProxiedPlayer proxiedPlayer) {
            operator   = proxiedPlayer.getUniqueId();
        }

        try {
            handle(commandSender, player, operator, guarantor, train, description);
        }catch (ComponentMessageException e){
            plugin.adventure().sender(commandSender).sendMessage(e.getComponentMessage());
        }
    }

    private void handle(CommandSender commandSender, String invited, UUID operator, String guarantor, String train, String description) throws ComponentMessageException {
        UUID invitedUUID = nameMapper.getUUID(invited).join().orElseThrow(()->new ComponentMessageException(plugin.text("general.player-not-exists", invited)));
        UUID guarantorUUID = new UUID(0,0);
        if(guarantor != null && !guarantor.equalsIgnoreCase("Console")){
            guarantorUUID =  nameMapper.getUUID(guarantor).join().orElseThrow(()->new ComponentMessageException(plugin.text("general.player-not-exists", guarantor)));
        }
        plugin.getWhitelistManager().addWhitelist(new WhitelistRecord(
                0,
                Instant.now(),
                invitedUUID,
                operator,
                guarantorUUID,
                train,
                description,
                0,
                null,
                null
        )).thenAccept(r -> {
            if (r <= 0) {
                plugin.adventure().sender(commandSender).sendMessage(plugin.text("wladd.error-no-updates", invited));
                return;
            }
            plugin.adventure().sender(commandSender).sendMessage(plugin.text("wladd.success", invited, r));
        }).exceptionally(err -> {
            err.printStackTrace();
            plugin.adventure().sender(commandSender).sendMessage(plugin.text("general.internal-error", err.getMessage()));
            return null;
        });

    }

}
