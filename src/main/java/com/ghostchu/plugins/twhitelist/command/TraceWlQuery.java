package com.ghostchu.plugins.twhitelist.command;

import com.ghostchu.plugins.twhitelist.NameMapper;
import com.ghostchu.plugins.twhitelist.TraceWhitelist;
import com.ghostchu.plugins.twhitelist.manager.bean.WhitelistRecord;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class TraceWlQuery extends Command {
    private final TraceWhitelist plugin;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private final NameMapper nameMapper;

    public TraceWlQuery(TraceWhitelist plugin, NameMapper nameMapper) {
        super("wlquery", "twhitelist.query");
        this.plugin = plugin;
        this.nameMapper =nameMapper;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length < 1) {
            plugin.adventure().sender(commandSender).sendMessage(plugin.text("wlquery.bad-arguments"));
            return;
        }
        String player = strings[0];
        Optional<UUID> uuid = nameMapper.getUUID(player).join();
        if(uuid.isEmpty()){
            plugin.adventure().sender(commandSender).sendMessage(plugin.text("wlquery.no-data"));
            return;
        }
        plugin.getWhitelistManager().queryWhitelist(uuid.get())
                .thenAccept(list -> {
                    if (list.isEmpty()) {
                        plugin.adventure().sender(commandSender).sendMessage(plugin.text("wlquery.no-data"));
                        return;
                    }
                    for (WhitelistRecord whitelistRecord : list) {
                        long id = whitelistRecord.getId();
                        String time = sdf.format(new Date(whitelistRecord.getTime().toEpochMilli()));
                        Component status = whitelistRecord.getDeleteAt() == 0 ? plugin.text("wlquery.valid") : plugin.text("wlquery.invalid");
                        Component guarantor = whitelistRecord.getGuarantor() == null ? plugin.text("wlquery.no-guarantor") : Component.text(nameMapper.getUsername(whitelistRecord.getGuarantor()).join().orElse("Unknown"));
                        UUID operator = whitelistRecord.getOperator();
                        String train = whitelistRecord.getTrain();
                        Component description = Component.text(whitelistRecord.getDescription());
                        Component deleteAt = whitelistRecord.getDeleteAt() == 0 ? plugin.text("wlquery.no-delete-at") : Component.text(sdf.format(whitelistRecord.getDeleteAt()));
                        Component deleteOperator = whitelistRecord.getDeleteOperator() == null ? plugin.text("wlquery.no-delete-operator") : Component.text(nameMapper.getUsername(whitelistRecord.getDeleteOperator()).join().orElse("Unknown"));
                        Component deleteReason = whitelistRecord.getDeleteReason() == null ? plugin.text("wlquery.no-delete-reason") : Component.text(whitelistRecord.getDeleteReason());
                        plugin.adventure().sender(commandSender).sendMessage(plugin.text("wlquery.entry", id, time, status, guarantor, operator, train, description, deleteAt, deleteOperator, deleteReason));
                    }
                })
                .exceptionally(err -> {
                    err.printStackTrace();
                    plugin.adventure().sender(commandSender).sendMessage(plugin.text("general.internal-error", err.getMessage()));
                    return null;
                });


    }
}
