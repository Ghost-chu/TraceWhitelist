package com.ghostchu.plugins.riawhitelist.manager.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

@AllArgsConstructor
@Data
public class WhitelistRecord {
    private long id;
    @NotNull
    private Instant time;
    @NotNull
    private String player;
    @NotNull
    private String email;
    @NotNull
    private String contact;
    @NotNull
    private String operator;
    @Nullable
    private String guarantor;
    @Nullable
    private String train;
    @NotNull
    private String description;
    private long deleteAt;
    @Nullable
    private String deleteReason;
    @Nullable
    private String deleteOperator;
}
