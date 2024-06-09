package com.ghostchu.plugins.twhitelist;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

public class PlayerFetcher {

    public static final String uuidURL = "https://sessionserver.mojang.com/session/minecraft/profile/";
    public static final String nameURL = "https://api.mojang.com/users/profiles/minecraft/";

    public static JsonObject getPlayerJson(UUID uuid, String name){
        try {
            URL url;

            if (name != null) {
                url = new URL(nameURL + name);
            } else {
                url = new URL(uuidURL + uuid);
            }
            URLConnection urlConnection = url.openConnection();

            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(urlConnection.getInputStream()));
            String line;
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            bufferedReader.close();
            if (!stringBuilder.toString().startsWith("{")) {
                return null;
            }
            return JsonParser.parseString(stringBuilder.toString()).getAsJsonObject();
        }catch (IOException e){
            return null;
        }
    }

    public static UUID getUUID(String name, UUID def) {
        JsonObject object = getPlayerJson(null, name);
        if (object == null) {
            return def;
        }
        String uuidTemp = object.get("id").getAsString();
        StringBuilder uuid = new StringBuilder();
        for (int i = 0; i <= 31; i++) {
            uuid.append(uuidTemp.charAt(i));
            if (i == 7 || i == 11 || i == 15 || i == 19) {
                uuid.append("-");
            }
        }
        return UUID.fromString(uuid.toString());
    }

    public static String getName(UUID uuid, String def) {
        JsonObject object = getPlayerJson(uuid, null);
        if (object != null) {
            return object.get("name").getAsString();
        }
        return def;
    }

}
 