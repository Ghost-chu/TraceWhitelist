package com.ghostchu.plugins.riawhitelist.command;

import com.ghostchu.plugins.riawhitelist.RIAWhitelist;
import com.ghostchu.plugins.riawhitelist.manager.bean.WhitelistRecord;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RIAWlQuery extends Command {
    private final RIAWhitelist plugin;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    public RIAWlQuery(RIAWhitelist plugin) {
        super("riawlquery", "riawhitelist.query", "riawhitelistquery", "rwlquery", "riawlcheck", "riawhitelistcheck", "rwlcheck");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender commandSender, String[] strings) {
        if (strings.length < 1) {
            plugin.adventure().sender(commandSender).sendMessage(plugin.text("wlquery.bad-arguments"));
            return;
        }
        String player = strings[0];
        plugin.getWhitelistManager().queryWhitelist(player)
                .thenAccept(list -> {
                    if (list.isEmpty()) {
                        plugin.adventure().sender(commandSender).sendMessage(plugin.text("wlquery.no-data"));
                        return;
                    }
                    for (WhitelistRecord whitelistRecord : list) {
                        long id = whitelistRecord.getId();
                        String time = sdf.format(new Date(whitelistRecord.getTime().toEpochMilli()));
                        Component status = whitelistRecord.getDeleteAt() == 0 ? plugin.text("wlquery.valid") : plugin.text("wlquery.invalid");
                        Component guarantor = whitelistRecord.getGuarantor() == null ? plugin.text("wlquery.no-guarantor") : Component.text(whitelistRecord.getGuarantor());
                        String operator = whitelistRecord.getOperator();
                        String train = whitelistRecord.getTrain();
                        Component description = Component.text(whitelistRecord.getDescription());
                        Component deleteAt = whitelistRecord.getDeleteAt() == 0 ? plugin.text("wlquery.no-delete-at") : Component.text(sdf.format(whitelistRecord.getDeleteAt()));
                        Component deleteOperator = whitelistRecord.getDeleteOperator() == null ? plugin.text("wlquery.no-delete-operator") : Component.text(whitelistRecord.getDeleteOperator());
                        Component deleteReason = whitelistRecord.getDeleteReason() == null ? plugin.text("wlquery.no-delete-reason") : Component.text(whitelistRecord.getDeleteReason());
                        plugin.adventure().sender(commandSender).sendMessage(plugin.text("wlquery.entry", id, time, status, guarantor, operator, train, description, deleteAt, deleteOperator, deleteReason, whitelistRecord.getContact()));
                    }
                })
                .exceptionally(err -> {
                    err.printStackTrace();
                    plugin.adventure().sender(commandSender).sendMessage(plugin.text("general.internal-error", err.getMessage()));
                    return null;
                });


    }
}
