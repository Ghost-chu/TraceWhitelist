package com.ghostchu.plugins.twhitelist.manager.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FastWhitelistQuery {
    private boolean whitelisted;
    private long recordId;
}
