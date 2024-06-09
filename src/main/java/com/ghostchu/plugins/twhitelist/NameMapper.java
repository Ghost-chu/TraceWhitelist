package com.ghostchu.plugins.twhitelist;

import cc.carm.lib.easysql.api.SQLManager;
import cc.carm.lib.easysql.api.SQLQuery;
import com.ghostchu.plugins.twhitelist.database.DataTables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class NameMapper {
    private final SQLManager db;
    private final Cache<UUID,Optional<String>> u2sCache =CacheBuilder.newBuilder()
            .expireAfterAccess(3, TimeUnit.DAYS)
            .softValues()
            .build();
    private final Cache<String,Optional<UUID>> s2uCache =CacheBuilder.newBuilder()
            .expireAfterAccess(3, TimeUnit.DAYS)
            .softValues()
            .build();

    public NameMapper(SQLManager databaseManager){
        this.db = databaseManager;
    }

    public void update(UUID uuid,String name){
        s2uCache.put(name,Optional.of(uuid));
        u2sCache.put(uuid,Optional.of(name));
        DataTables.USERNAME_MAPPING
                .createReplace()
                .setColumnNames("uuid","name")
                .setParams(uuid.toString(),name)
                .executeAsync();
    }

    public CompletableFuture<Optional<String>> getUsername(UUID uuid){
       return CompletableFuture.supplyAsync(()->{
           try {
               return u2sCache.get(uuid,()->username(uuid));
           } catch (ExecutionException e) {
               throw new RuntimeException(e);
           }
       });
    }

    private Optional<String> username(UUID uuid){
        try(SQLQuery query = DataTables.USERNAME_MAPPING.createQuery()
                .addCondition("uuid", uuid.toString())
                .setLimit(1)
                .build()
                .execute()){
            ResultSet rs = query.getResultSet();
            if(rs.next()){
                return Optional.of(rs.getString("name"));
            }
            return Optional.ofNullable(PlayerFetcher.getName(uuid, null));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public CompletableFuture<Optional<UUID>> getUUID(String username){
        return CompletableFuture.supplyAsync(()->{
            try {
                return s2uCache.get(username,()->uuid(username));
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private Optional<UUID> uuid(String username){
        try(SQLQuery query = DataTables.USERNAME_MAPPING.createQuery()
                .addCondition("name", username)
                .setLimit(1)
                .build()
                .execute()){
            ResultSet rs = query.getResultSet();
            if(rs.next()){
                return Optional.of(UUID.fromString(rs.getString("uuid")));
            }
            return Optional.ofNullable(PlayerFetcher.getUUID(username, null));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
