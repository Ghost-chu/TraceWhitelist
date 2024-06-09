package com.ghostchu.plugins.twhitelist.manager.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@Data
public class WhitelistRecord {
    private long id;
    @NotNull
    private Instant time;
    @NotNull
    private UUID player;
    @NotNull
    private UUID operator;
    @Nullable
    private UUID guarantor;
    @Nullable
    private String train;
    @NotNull
    private String description;
    private long deleteAt;
    @Nullable
    private String deleteReason;
    @Nullable
    private UUID deleteOperator;
}
