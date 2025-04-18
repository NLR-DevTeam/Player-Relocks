package cn.xiaym.relocks.data;

import cn.xiaym.relocks.PlayerRelocks;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GlobalDataManager {
    private final static HashMap<UUID, PlayerData> PLAYER_DATA_MAP = new HashMap<>();
    private static Path savePath;

    public static PlayerData forPlayer(UUID uuid) {
        return PLAYER_DATA_MAP.computeIfAbsent(uuid, it -> new PlayerData());
    }

    public static void init(Path savePath) throws IOException {
        GlobalDataManager.savePath = savePath;
        PLAYER_DATA_MAP.clear();

        if (Files.notExists(savePath)) {
            Files.createFile(savePath);
            save();
        } else {
            JsonObject jsonObject = JsonParser.parseString(Files.readString(savePath)).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                UUID uuid = UUID.fromString(entry.getKey());
                PlayerData playerData = PlayerData.deserialize(entry.getValue().getAsJsonObject());

                PLAYER_DATA_MAP.put(uuid, playerData);
            }
        }
    }

    public static void save() throws IOException {
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<UUID, PlayerData> entry : PLAYER_DATA_MAP.entrySet()) {
            jsonObject.add(entry.getKey().toString(), entry.getValue().serialize());
        }

        Files.writeString(savePath, PlayerRelocks.GSON.toJson(jsonObject));
    }
}
