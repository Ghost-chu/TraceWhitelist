package com.ghostchu.plugins.riawhitelist.manager.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor
@Data
public class FastWhitelistQuery {
    private boolean allowed;
    private boolean whitelisted;
    @Nullable
    private String correctWhitelistedName;
    private long recordId;
}
